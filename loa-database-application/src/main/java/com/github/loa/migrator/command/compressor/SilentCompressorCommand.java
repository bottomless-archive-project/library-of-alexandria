package com.github.loa.migrator.command.compressor;

import com.github.loa.compression.configuration.CompressionConfigurationProperties;
import com.github.loa.compression.domain.DocumentCompression;
import com.github.loa.document.service.DocumentManipulator;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.domain.DocumentStatus;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.vault.service.VaultDocumentManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty("silent-compressor")
public class SilentCompressorCommand implements CommandLineRunner {

    private final DocumentEntityFactory documentEntityFactory;
    private final VaultDocumentManager vaultDocumentManager;
    private final DocumentManipulator documentManipulator;
    private final CompressionConfigurationProperties compressionConfigurationProperties;

    @Override
    public void run(final String... args) {
        documentEntityFactory.getDocumentEntities()
                .filter(DocumentEntity::isInVault)
                .filter(this::shouldRecompress)
                .forEach(documentEntity -> {
                    log.info("Migrating archived document " + documentEntity.getId() + " from "
                            + documentEntity.getCompression() + " compression to "
                            + compressionConfigurationProperties.getAlgorithm() + ".");

                    final byte[] documentContent = vaultDocumentManager.readDocument(documentEntity);

                    vaultDocumentManager.removeDocument(documentEntity);
                    vaultDocumentManager.archiveDocument(documentEntity, new ByteArrayInputStream(documentContent));

                    documentManipulator.updateCompression(documentEntity.getId(),
                            compressionConfigurationProperties.getAlgorithm());
                });
    }

    private boolean shouldRecompress(final DocumentEntity documentEntity) {
        return documentEntity.getCompression() != compressionConfigurationProperties.getAlgorithm();
    }
}
