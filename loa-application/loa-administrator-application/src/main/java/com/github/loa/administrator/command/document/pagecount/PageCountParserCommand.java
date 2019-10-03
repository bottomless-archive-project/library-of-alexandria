package com.github.loa.administrator.command.document.pagecount;

import com.github.loa.administrator.command.document.pagecount.domain.PageCountDocument;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.vault.client.service.VaultClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty("page-count-parser")
public class PageCountParserCommand implements CommandLineRunner {

    private final PageCountParserConfigurationProperties pageCountParserConfigurationProperties;
    private final DocumentEntityFactory documentEntityFactory;
    private final VaultClientService vaultClientService;
    private final Scheduler pageCountScheduler;
    private final PageCountProcessor pageCountProcessor;

    @Override
    public void run(final String... args) {
        log.info("Starting the document page parser command.");

        documentEntityFactory.getDocumentEntities()
                .limitRate(100)
                .parallel(pageCountParserConfigurationProperties.getParallelismLevel())
                .runOn(pageCountScheduler)
                .filter(DocumentEntity::isArchived)
                .filter(DocumentEntity::isPdf)
                // Request should be limited otherwise the Spring webclient would eat up all of the open connections
                .flatMap(this::buildDocument, false, 5)
                .subscribe(pageCountProcessor::processDocument);

    }

    private Mono<PageCountDocument> buildDocument(final DocumentEntity documentEntity) {
        return vaultClientService.queryDocument(documentEntity)
                .map(documentContent -> PageCountDocument.builder()
                        .documentEntity(documentEntity)
                        .documentContents(documentContent)
                        .build()
                );
    }
}
