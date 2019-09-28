package com.github.loa.administrator.command.document;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.vault.client.service.VaultClientService;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.util.Precision;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;

/**
 * This command will go through all of the {@link DocumentEntity}s available in the database and validate if they are
 * openable and parsable.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty("document-validator")
public class DocumentValidatorCommand implements CommandLineRunner {

    private final static String SCHEDULER_NAME = "document-validator-scheduler";

    private final DocumentEntityFactory documentEntityFactory;
    private final VaultClientService vaultClientService;
    private final DocumentValidatorConfigurationProperties documentValidatorConfigurationProperties;

    private final AtomicLong processedDocumentCount = new AtomicLong();
    private final DoubleAdder doubleAdder = new DoubleAdder();

    /**
     * Runs the command.
     *
     * @param args the command arguments, none yet
     */
    @Override
    public void run(final String... args) {
        log.info("Starting the document validator command.");

        documentEntityFactory.getDocumentEntities()
                .limitRate(100)
                .parallel(documentValidatorConfigurationProperties.getParallelismLevel())
                .runOn(newScheduler())
                .filter(DocumentEntity::isArchived)
                .filter(DocumentEntity::isPdf)
                // Request should be limited otherwise the Spring webclient would eat up all of the open connections
                .flatMap(this::buildDocument, false, 5)
                .subscribe(this::processDocument);
    }

    private Mono<Document> buildDocument(final DocumentEntity documentEntity) {
        return vaultClientService.queryDocument(documentEntity)
                .map(documentContent -> Document.builder()
                        .documentEntity(documentEntity)
                        .documentContents(documentContent)
                        .build()
                );
    }

    private void processDocument(final Document document) {
        try (final PDDocument pdfDocument = PDDocument.load(document.getDocumentContents())) {
            // Doing nothing when the pdf is valid
        } catch (IOException e) {
            doubleAdder.add(document.getSize());

            log.info(document.documentEntity.getId() + " with size: " + Precision.round(document.getSize(), 2)
                    + " mb and total size: " + Precision.round(doubleAdder.doubleValue(), 2)
                    + " mb is an invalid pdf! [" + e.getClass() + "]: " + e.getMessage() + ".");

            vaultClientService.removeDocument(document.documentEntity)
                    .doOnNext(response -> log.info("Removed document with id: " + document.documentEntity.getId()))
                    .subscribe();
        }

        final long processedDocument = processedDocumentCount.incrementAndGet();
        if (processedDocument % 100 == 0) {
            log.info("Processed " + processedDocument + " documents.");
        }
    }

    private Scheduler newScheduler() {
        return Schedulers.newParallel(SCHEDULER_NAME, documentValidatorConfigurationProperties.getParallelismLevel());
    }

    @Getter
    @Builder
    private static class Document {

        private final DocumentEntity documentEntity;
        private final byte[] documentContents;

        private double getSize() {
            return (double) documentContents.length / (double) (1024L * 1024L);
        }
    }
}
