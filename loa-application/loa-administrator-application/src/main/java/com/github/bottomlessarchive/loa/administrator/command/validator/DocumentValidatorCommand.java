package com.github.bottomlessarchive.loa.administrator.command.validator;

import com.github.bottomlessarchive.loa.parser.domain.ParsingException;
import com.github.bottomlessarchive.loa.parser.service.DocumentDataParser;
import com.github.bottomlessarchive.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.bottomlessarchive.loa.vault.client.service.VaultClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "loa.command", havingValue = "document-validator")
public class DocumentValidatorCommand implements CommandLineRunner {

    private final DocumentDataParser documentDataParser;
    private final DocumentEntityFactory documentEntityFactory;
    private final VaultClientService vaultClientService;

    private final AtomicInteger processedCount = new AtomicInteger();
    private final AtomicInteger errorCount = new AtomicInteger();

    @Override
    public void run(final String... args) {
        documentEntityFactory.getDocumentEntitiesSync()
                .forEach(documentEntity -> {
                    final int processed = processedCount.incrementAndGet();

                    if (processed % 10 == 0) {
                        log.info("Processed: {} documents!", processed);
                    }

                    try (InputStream documentLocation = vaultClientService.queryDocument(documentEntity)) {
                        documentDataParser.parseDocumentMetadata(documentEntity.getId(), documentEntity.getType(), documentLocation);
                    } catch (IOException e) {
                        log.error("Failed to close vault response!", e);
                    } catch (ParsingException e) {
                        log.info("New error! Error count: {}.", errorCount.incrementAndGet());
                    }
                });
    }
}
