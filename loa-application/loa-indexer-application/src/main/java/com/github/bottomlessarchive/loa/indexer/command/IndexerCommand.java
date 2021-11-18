package com.github.bottomlessarchive.loa.indexer.command;

import com.github.bottomlessarchive.loa.document.service.DocumentManipulator;
import com.github.bottomlessarchive.loa.indexer.configuration.IndexerConfigurationProperties;
import com.github.bottomlessarchive.loa.indexer.service.index.domain.IndexingContext;
import com.github.bottomlessarchive.loa.parser.service.DocumentDataParser;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentStatus;
import com.github.bottomlessarchive.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.bottomlessarchive.loa.indexer.service.index.IndexerService;
import com.github.bottomlessarchive.loa.indexer.service.search.DocumentSearchService;
import com.github.bottomlessarchive.loa.vault.client.service.VaultClientService;
import com.github.bottomlessarchive.loa.vault.client.service.domain.VaultAccessException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.util.UUID;

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

    @SneakyThrows
    private Mono<Void> processDocument(final DocumentEntity documentEntity) {
        return vaultClientService.queryDocument(documentEntity)
                .reduce(new ByteArrayOutputStream(), (outputStream, value) -> {
                    try {
                        outputStream.write(value.asInputStream().readAllBytes());

                        DataBufferUtils.release(value);
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }

                    return outputStream;
                })
                .map(ByteArrayOutputStream::toByteArray)
                .publishOn(Schedulers.parallel())
                .map(documentContent -> documentDataParser.parseDocumentMetadata(
                        documentEntity.getId(), documentEntity.getType(), documentContent))
                .onErrorContinue((throwable, document) -> {
                    final UUID documentId = documentEntity.getId();

                    //TODO: This should be handled in the VaultClientService but we are unable to do so
                    // because of this onErrorContinue here (no sufficient operator in the reactive toolset).
                    if (throwable instanceof ConnectException) {
                        throw new VaultAccessException("Error while connecting to the vault for document: "
                                + documentId + "!", throwable);
                    }

                    documentManipulator.markCorrupt(documentId)
                            .subscribe();

                    log.info("Failed to parse document with id: {}!", documentId);
                })
                .map(documentMetadata -> IndexingContext.builder()
                        .id(documentMetadata.getId())
                        .author(documentMetadata.getAuthor())
                        .content(documentMetadata.getContent())
                        .date(documentMetadata.getDate())
                        .language(documentMetadata.getLanguage())
                        .pageCount(documentMetadata.getPageCount())
                        .title(documentMetadata.getTitle())
                        .type(documentMetadata.getType())
                        .build()
                )
                .doOnNext(indexerService::indexDocument)
                .then();
    }
}
