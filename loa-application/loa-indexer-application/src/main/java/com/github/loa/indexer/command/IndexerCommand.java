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

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class IndexerCommand implements CommandLineRunner {

    private final DocumentEntityFactory documentEntityFactory;
    private final VaultClientService vaultClientService;
    private final IndexerConfigurationProperties indexerConfigurationProperties;
    private final IndexerService indexerService;
    private final DocumentParser documentParser;
    private final DocumentManipulator documentManipulator;

    @Override
    public void run(final String... args) {
        log.info("Initializing document indexing.");

        documentEntityFactory.getDocumentEntity(DocumentStatus.DOWNLOADED)
                .filter(this::filterDocument)
                .flatMap(this::buildDocument, indexerConfigurationProperties.getConcurrentIndexerThreads())
                .flatMap(this::processDocument)
                .subscribe();
    }

    private boolean filterDocument(final DocumentEntity documentEntity) {
        if (documentEntity.getFileSize() > indexerConfigurationProperties.getMaximumFileSize()) {
            log.info("Skipping document {} because it's size is too big.", documentEntity.getId());

            documentManipulator.markIndexFailure(documentEntity.getId()).subscribe();

            return false;
        }

        return true;
    }

    private Mono<Void> processDocument(final IndexDocument indexDocument) {
        final DocumentEntity documentEntity = indexDocument.getDocumentEntity();

        if (documentEntity.isPdf()) {
            return Mono.just(indexDocument)
                    .flatMap(document -> parseDocument(indexDocument))
                    .flatMap(pdfDocument -> updatePageCount(documentEntity, pdfDocument))
                    .flatMap(pdfDocument -> {
                        final Integer pageCount = pdfDocument.getNumberOfPages();

                        return Mono.just(pdfDocument)
                                .doOnNext(pdDocument -> {
                                    try {
                                        pdfDocument.close();
                                    } catch (IOException e) {
                                        log.error("Unable to close document!", e);
                                    }
                                })
                                .thenReturn(pageCount);
                    })
                    .doOnNext(pageCount -> indexerService.indexDocuments(documentEntity, pageCount))
                    .then();
        } else {
            return Mono.just(indexDocument)
                    .doOnNext(pdfDocument -> indexerService.indexDocuments(documentEntity, -1))
                    .then();
        }
    }

    private Mono<PDDocument> updatePageCount(final DocumentEntity documentEntity, final PDDocument pdfDocument) {
        return documentManipulator.updatePageCount(documentEntity.getId(), pdfDocument.getNumberOfPages())
                .thenReturn(pdfDocument);
    }

    private Mono<PDDocument> parseDocument(final IndexDocument indexDocument) {
        return Mono.fromSupplier(() -> documentParser.parseDocument(indexDocument.getDocumentContents()))
                .onErrorResume(error -> vaultClientService.removeDocument(indexDocument.getDocumentEntity())
                        .doOnNext(response -> log.info("Removed document with id: {}.",
                                indexDocument.getDocumentEntity().getId()))
                        .then(Mono.empty())
                );
    }

    private Mono<IndexDocument> buildDocument(final DocumentEntity documentEntity) {
        return vaultClientService.queryDocument(documentEntity)
                .doOnError(error -> log.error("Error while downloading the document!", error))
                .map(documentContent -> IndexDocument.builder()
                        .documentEntity(documentEntity)
                        .documentContents(documentContent)
                        .build()
                )
                .onErrorResume(error -> Mono.empty());
    }
}
