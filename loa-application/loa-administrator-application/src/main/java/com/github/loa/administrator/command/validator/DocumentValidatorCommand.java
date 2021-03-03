package com.github.loa.administrator.command.validator;

import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.parser.service.DocumentDataParser;
import com.github.loa.vault.client.service.VaultClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty("document-validator")
public class DocumentValidatorCommand implements CommandLineRunner {

    private final DocumentDataParser documentDataParser;
    private final DocumentEntityFactory documentEntityFactory;
    private final VaultClientService vaultClientService;

    private final AtomicInteger processedCount = new AtomicInteger();
    private final AtomicInteger errorCount = new AtomicInteger();

    @Override
    public void run(final String... args) {
        documentEntityFactory.getDocumentEntities()
                .doOnNext(documentEntity -> {
                    final int processed = processedCount.incrementAndGet();

                    if (processed % 10 == 0) {
                        log.info("Processed: {} documents!", processed);
                    }
                })
                .flatMap(documentEntity ->
                        vaultClientService.queryDocument(documentEntity)
                                .publishOn(Schedulers.parallel())
                                .map(documentContent -> documentDataParser.parseDocumentMetadata(documentEntity.getId(),
                                        documentEntity.getType(), documentContent))
                                .doOnError(error -> log.info("New error! Error count: {}.", errorCount.incrementAndGet()))
                                .onErrorResume(error -> Mono.empty())
                )
                .subscribe();
    }
}
