package com.github.loa.indexer.command;

import com.github.loa.document.service.DocumentManipulator;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.domain.DocumentStatus;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.indexer.command.domain.IndexDocument;
import com.github.loa.indexer.configuration.IndexerConfigurationProperties;
import com.github.loa.indexer.service.index.IndexerService;
import com.github.loa.vault.client.service.VaultClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class IndexerCommand implements CommandLineRunner {

    private static final String SCHEDULER_NAME = "indexer-scheduler";

    private final DocumentEntityFactory documentEntityFactory;
    private final VaultClientService vaultClientService;
    private final IndexerConfigurationProperties indexerConfigurationProperties;
    private final IndexerService indexerService;
    private final DocumentManipulator documentManipulator;

    @Override
    public void run(final String... args) {
        log.info("Initializing document indexing.");

        documentEntityFactory.getDocumentEntity(DocumentStatus.DOWNLOADED)
                .parallel(indexerConfigurationProperties.getConcurrentIndexerThreads())
                .runOn(newScheduler(indexerConfigurationProperties))
                .flatMap(this::buildDocument, false, 5)
                .subscribe(this::processDocument);
    }

    private Scheduler newScheduler(final IndexerConfigurationProperties indexerConfigurationProperties) {
        return Schedulers.newParallel(SCHEDULER_NAME, indexerConfigurationProperties.getConcurrentIndexerThreads());
    }

    private void processDocument(final IndexDocument indexDocument) {
        final DocumentEntity documentEntity = indexDocument.getDocumentEntity();

        if (documentEntity.isPdf()) {
            try (final PDDocument pdfDocument = PDDocument.load(indexDocument.getDocumentContents())) {
                // When the document is valid parse it's page count and update it
                final int pageCount = pdfDocument.getNumberOfPages();

                documentManipulator.updatePageCount(indexDocument.getDocumentEntity().getId(), pageCount);

                // Index the document
                if (documentEntity.getFileSize() < indexerConfigurationProperties.getMaximumFileSize()) {
                    indexerService.indexDocuments(documentEntity, pageCount);
                } else {
                    log.info("Skipping document {} because it's size is too big.", documentEntity.getId());

                    documentManipulator.markIndexFailure(documentEntity.getId());
                }
            } catch (IOException e) {
                // When the document is invalid remove it
                vaultClientService.removeDocument(indexDocument.getDocumentEntity())
                        .doOnNext(response -> log.info("Removed document with id: {}.", documentEntity.getId()))
                        .subscribe();
            }
        } else {
            // Index the document
            if (documentEntity.getFileSize() < indexerConfigurationProperties.getMaximumFileSize()) {
                indexerService.indexDocuments(documentEntity, -1);
            } else {
                log.info("Skipping document {} because it's size is too big.", documentEntity.getId());

                documentManipulator.markIndexFailure(documentEntity.getId());
            }
        }
    }

    private Mono<IndexDocument> buildDocument(final DocumentEntity documentEntity) {
        return vaultClientService.queryDocument(documentEntity)
                .map(documentContent -> IndexDocument.builder()
                        .documentEntity(documentEntity)
                        .documentContents(documentContent)
                        .build()
                );
    }
}
