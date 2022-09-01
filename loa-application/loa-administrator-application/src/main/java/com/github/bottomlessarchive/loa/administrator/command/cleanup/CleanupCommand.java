package com.github.bottomlessarchive.loa.administrator.command.cleanup;

import com.github.bottomlessarchive.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.vault.client.service.VaultClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "loa.command", havingValue = "cleanup")
public class CleanupCommand implements CommandLineRunner {

    private final DocumentEntityFactory documentEntityFactory;
    private final VaultClientService vaultClientService;

    @Override
    public void run(final String... args) {
        log.info("Started to run the cleanup command!");

        documentEntityFactory.getDocumentEntitiesSync()
                .filter(DocumentEntity::isCorrupt)
                .forEach(this::cleanup);
    }

    private void cleanup(final DocumentEntity documentEntity) {
        log.info("Cleaning up document entity: {}.", documentEntity);

        vaultClientService.deleteDocument(documentEntity);
    }
}
