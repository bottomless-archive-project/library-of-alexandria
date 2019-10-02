package com.github.loa.administrator.command.document.validator;

import com.github.loa.administrator.command.document.validator.domain.ValidatorDocument;
import com.github.loa.document.service.DocumentCollector;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.vault.client.service.VaultClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.util.Precision;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentValidatorProcessor {

    private final VaultClientService vaultClientService;
    private final DocumentCollector validatorDocumentCollector;

    //TODO: Why stateful???
    private final AtomicLong processedDocumentCounter = new AtomicLong();
    private final DoubleAdder doubleAdder = new DoubleAdder();

    public void processDocument(final ValidatorDocument document) {
        final DocumentEntity documentEntity = document.getDocumentEntity();

        try (final PDDocument pdfDocument = PDDocument.load(document.getDocumentContents())) {
            // Doing nothing when the pdf is valid
        } catch (IOException e) {
            doubleAdder.add(document.getSize());

            log.info(documentEntity.getId() + " with size: " + Precision.round(document.getSize(), 2)
                    + " mb and total size: " + Precision.round(doubleAdder.doubleValue(), 2)
                    + " mb is an invalid pdf! [" + e.getClass() + "]: " + e.getMessage() + ".");

            vaultClientService.removeDocument(documentEntity)
                    .doOnNext(response -> log.info("Removed document with id: " + documentEntity.getId()))
                    .subscribe();
        }

        final long processedDocumentCount = processedDocumentCounter.incrementAndGet();
        if (processedDocumentCount % 100 == 0) {
            log.info("Processed " + processedDocumentCount + " documents.");
        }

        if (processedDocumentCount % 10000 == 0) {
            validatorDocumentCollector.persist();
        }

        validatorDocumentCollector.insert(documentEntity);
    }
}
