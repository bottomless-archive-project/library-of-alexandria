package com.github.loa.administrator.command.document.validator;

import com.github.loa.administrator.command.document.validator.domain.ValidatorDocument;
import com.github.loa.document.service.DocumentCollector;
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

/**
 * This command will go through all of the {@link DocumentEntity}s available in the database and validate if they are
 * openable and parsable.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty("document-validator")
public class DocumentValidatorCommand implements CommandLineRunner {

    private final DocumentEntityFactory documentEntityFactory;
    private final VaultClientService vaultClientService;
    private final DocumentValidatorConfigurationProperties documentValidatorConfigurationProperties;
    private final DocumentCollector validatorDocumentCollector;
    private final Scheduler validatorScheduler;
    private final DocumentValidatorProcessor documentValidatorProcessor;

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
                .runOn(validatorScheduler)
                .filter(DocumentEntity::isArchived)
                .filter(DocumentEntity::isPdf)
                .filter(documentEntity -> !validatorDocumentCollector.contains(documentEntity))
                // Request should be limited otherwise the Spring webclient would eat up all of the open connections
                .flatMap(this::buildDocument, false, 5)
                .subscribe(documentValidatorProcessor::processDocument);
    }

    private Mono<ValidatorDocument> buildDocument(final DocumentEntity documentEntity) {
        return vaultClientService.queryDocument(documentEntity)
                .map(documentContent -> ValidatorDocument.builder()
                        .documentEntity(documentEntity)
                        .documentContents(documentContent)
                        .build()
                );
    }
}
