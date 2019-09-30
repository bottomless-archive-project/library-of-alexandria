package com.github.loa.administrator.command.document.pagecount;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.vault.client.service.VaultClientService;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty("page-count-parser")
public class PageCountParserCommand implements CommandLineRunner {

    private final static String SCHEDULER_NAME = "page-count-parser-scheduler";

    private final DocumentEntityFactory documentEntityFactory;
    private final VaultClientService vaultClientService;
    private final PageCountParserConfigurationProperties pageCountParserConfigurationProperties;

    @Override
    public void run(final String... args) {
        log.info("Starting the document page parser command.");

        documentEntityFactory.getDocumentEntities()
                .limitRate(100)
                .parallel(pageCountParserConfigurationProperties.getParallelismLevel())
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
            log.info(document.getDocumentEntity().getId() + " page count: " + pdfDocument.getNumberOfPages());
        } catch (IOException e) {
        }
    }

    private Scheduler newScheduler() {
        return Schedulers.newParallel(SCHEDULER_NAME, pageCountParserConfigurationProperties.getParallelismLevel());
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
