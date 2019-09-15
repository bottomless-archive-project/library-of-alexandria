package com.github.loa.administrator.command.document;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.domain.DocumentType;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.vault.client.service.VaultClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.util.Precision;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty("document-validator")
public class DocumentValidatorCommand implements CommandLineRunner {

    private final DocumentEntityFactory documentEntityFactory;
    private final VaultClientService vaultClientService;
    private final Semaphore semaphore = new Semaphore(5000);
    private final ExecutorService executorService = Executors.newFixedThreadPool(1000);
    private final AtomicLong processedDocumentCount = new AtomicLong();
    private final DoubleAdder doubleAdder = new DoubleAdder();

    @Override
    public void run(final String... args) {
        log.info("Starting the document validator command.");

        documentEntityFactory.getDocumentEntities()
                .parallel(10)
                .runOn(Schedulers.parallel())
                .doOnNext((documentEntity) -> {
                    try {
                        semaphore.acquire();
                    } catch (InterruptedException e) {
                        log.info("Failed to acquire permit!");
                    }
                })
                .filter(DocumentEntity::isArchived)
                .filter(documentEntity -> documentEntity.getType() == DocumentType.PDF)
                .doOnNext(this::processDocument);

        log.info("Stopped the document validator command.");
    }

    private void processDocument(final DocumentEntity documentEntity) {
        executorService.execute(() -> {
            final byte[] documentContents = vaultClientService.queryDocumentRaw(documentEntity);

            try (final PDDocument document = PDDocument.load(documentContents)) {
                //log.info(documentEntity.getId() + " is a valid pdf!");
            } catch (IOException e) {
                final double documentContentSize = (double) documentContents.length / (double) (1024L * 1024L);

                doubleAdder.add(documentContentSize);

                log.info(documentEntity.getId() + " with size: " + Precision.round(documentContentSize, 2) + " mb and total size: " + Precision.round(doubleAdder.doubleValue(), 2)
                        + " mb is an invalid pdf! [" + e.getClass() + "]: " + e.getMessage() + ".");

                removeDocument(documentEntity);
            }

            final long processedDocument = processedDocumentCount.incrementAndGet();
            if (processedDocument % 100 == 0) {
                log.info("Processed " + processedDocument + " documents.");
            }

            semaphore.release();
        });
    }

    private void removeDocument(final DocumentEntity documentEntity) {

    }
}
