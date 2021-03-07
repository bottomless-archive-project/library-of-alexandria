package com.github.loa.administrator.command.cleanup;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.vault.client.service.VaultClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty("cleanup")
@RequiredArgsConstructor
public class CleanupCommand implements CommandLineRunner {

    private final DocumentEntityFactory documentEntityFactory;
    private final VaultClientService vaultClientService;

    @Override
    public void run(final String... args) {
        documentEntityFactory.getDocumentEntities()
                .filter(DocumentEntity::isCorrupt)
                .flatMap(documentEntity ->
                        vaultClientService.deleteDocument(documentEntity)
                                .and(documentEntityFactory.removeDocumentEntity(documentEntity.getId()))
                )
                .subscribe();
    }
}
