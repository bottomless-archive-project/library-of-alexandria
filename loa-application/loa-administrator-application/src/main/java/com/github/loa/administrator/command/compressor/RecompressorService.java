package com.github.loa.administrator.command.compressor;

import com.github.loa.compression.configuration.CompressionConfigurationProperties;
import com.github.loa.document.service.DocumentManipulator;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.vault.service.VaultDocumentManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty("silent-compressor")
public class RecompressorService {

    private final VaultDocumentManager vaultDocumentManager;
    private final DocumentManipulator documentManipulator;
    private final CompressionConfigurationProperties compressionConfigurationProperties;

    public void recompress(final DocumentEntity documentEntity) {
        log.info("Migrating archived document " + documentEntity.getId() + " from "
                + documentEntity.getCompression() + " compression to "
                + compressionConfigurationProperties.getAlgorithm() + ".");

        try {
            //TODO: IOUtils.toByteArray could overflow!
            final byte[] documentContent = IOUtils.toByteArray(
                    vaultDocumentManager.readDocument(documentEntity));

            vaultDocumentManager.removeDocument(documentEntity);
            vaultDocumentManager.archiveDocument(documentEntity, new ByteArrayInputStream(documentContent));

            documentManipulator.updateCompression(documentEntity.getId(),
                    compressionConfigurationProperties.getAlgorithm());
        } catch (IOException e) {
            throw new RuntimeException("Unable to base64 encode document " + documentEntity.getId() + "!", e);
        }
    }
}
