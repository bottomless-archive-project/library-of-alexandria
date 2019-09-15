package com.github.loa.vault.service;

import com.github.loa.compression.configuration.CompressionConfigurationProperties;
import com.github.loa.compression.domain.DocumentCompression;
import com.github.loa.document.service.DocumentManipulator;
import com.github.loa.document.service.domain.DocumentEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecompressorService {

    private final VaultDocumentManager vaultDocumentManager;
    private final DocumentManipulator documentManipulator;
    private final CompressionConfigurationProperties compressionConfigurationProperties;

    public void recompress(final DocumentEntity documentEntity, final DocumentCompression documentCompression) {
        log.info("Migrating archived document " + documentEntity.getId() + " from "
                + documentEntity.getCompression() + " compression to "
                + compressionConfigurationProperties.getAlgorithm() + ".");

        try (final InputStream documentContentInputStream = vaultDocumentManager.readDocument(documentEntity)
                .getInputStream()) {
            final byte[] documentContent = documentContentInputStream.readAllBytes();

            vaultDocumentManager.removeDocument(documentEntity);
            vaultDocumentManager.archiveDocument(documentEntity, new ByteArrayResource(documentContent));

            documentManipulator.updateCompression(documentEntity.getId(), documentCompression);
        } catch (IOException e) {
            throw new RuntimeException("Unable to base64 encode document " + documentEntity.getId() + "!", e);
        }
    }
}
