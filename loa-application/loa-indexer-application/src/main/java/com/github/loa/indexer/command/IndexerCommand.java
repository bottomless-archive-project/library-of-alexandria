package com.github.loa.indexer.command;

import com.github.loa.document.service.DocumentManipulator;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.domain.DocumentStatus;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.indexer.configuration.IndexerConfigurationProperties;
import com.github.loa.indexer.service.index.IndexerService;
import com.github.loa.indexer.service.search.DocumentSearchService;
import com.github.loa.parser.service.DocumentDataParser;
import com.github.loa.vault.client.service.VaultClientService;
import com.github.loa.vault.client.service.domain.VaultAccessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.ConnectException;

@Slf4j
@Component
@RequiredArgsConstructor
public class IndexerCommand implements CommandLineRunner {

    private final DocumentEntityFactory documentEntityFactory;
    private final IndexerConfigurationProperties indexerConfigurationProperties;
    private final IndexerService indexerService;
    private final VaultClientService vaultClientService;
    private final DocumentDataParser documentDataParser;
    private final DocumentManipulator documentManipulator;
    private final DocumentSearchService documentSearchService;

    @Override
    public void run(final String... args) {
        if (!documentSearchService.isSearchEngineInitialized()) {
            log.info("Initializing the search engine!");

            documentSearchService.initializeSearchEngine();
        }

        log.info("Start document indexing.");

        documentEntityFactory.getDocumentEntity(DocumentStatus.DOWNLOADED)
                .flatMap(this::processDocument, indexerConfigurationProperties.getConcurrentIndexerThreads())
                .subscribe();
    }

    private Mono<Void> processDocument(final DocumentEntity documentEntity) {
        return vaultClientService.queryDocument(documentEntity)
                .reduce(InputStream.nullInputStream(),
                        (s, d) -> new SequenceInputStream(s, d.asInputStream()))
                .publishOn(Schedulers.parallel())
                .flatMap(documentContent ->
                        Mono.using(
                                () -> documentContent,
                                (content) -> Mono.just(documentDataParser.parseDocumentMetadata(
                                        documentEntity.getId(), documentEntity.getType(), content)),
                                (content) -> {
                                    try {
                                        content.close();
                                    } catch (IOException e) {
                                        log.error("Failed to close vault response!", e);
                                    }
                                })
                )
                .onErrorContinue((throwable, document) -> {
                    //TODO: This should be handled in the VaultClientService but we are unable to do so
                    // because of this onErrorContinue here (no sufficient operator in the reactive toolset).
                    if (throwable instanceof ConnectException) {
                        throw new VaultAccessException("Error while connecting to the vault for document: "
                                + documentEntity.getId() + "!", throwable);
                    }

                    documentManipulator.markCorrupt(documentEntity.getId())
                            .subscribe();

                    log.info("Failed to parse document with id: {}!", documentEntity.getId());
                })
                .doOnNext(indexerService::indexDocuments)
                .then();
    }
}
