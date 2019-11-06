package com.github.loa.indexer.command;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.domain.DocumentStatus;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.indexer.configuration.IndexerConfigurationProperties;
import com.github.loa.parser.service.DocumentDataParser;
import com.github.loa.indexer.service.index.IndexerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class IndexerCommand implements CommandLineRunner {

    private final DocumentEntityFactory documentEntityFactory;
    private final IndexerConfigurationProperties indexerConfigurationProperties;
    private final IndexerService indexerService;
    private final DocumentDataParser documentDataParser;

    @Override
    public void run(final String... args) {
        log.info("Initializing document indexing.");

        documentEntityFactory.getDocumentEntity(DocumentStatus.DOWNLOADED)
                .flatMap(this::processDocument, indexerConfigurationProperties.getConcurrentIndexerThreads())
                .subscribe();
    }

    private Mono<Void> processDocument(final DocumentEntity documentEntity) {
        return documentDataParser.parseDocumentData(documentEntity)
                .doOnNext(indexerService::indexDocuments)
                .then();
    }
}
