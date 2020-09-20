package com.github.loa.vault.service;

import com.github.loa.compression.configuration.CompressionConfigurationProperties;
import com.github.loa.compression.domain.DocumentCompression;
import com.github.loa.document.service.DocumentManipulator;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.vault.domain.exception.StorageAccessException;
import com.github.loa.vault.service.backend.service.VaultDocumentStorage;
import com.github.loa.vault.service.location.VaultLocation;
import com.github.loa.vault.service.location.VaultLocationFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecompressorService {

    private final VaultDocumentManager vaultDocumentManager;
    private final DocumentManipulator documentManipulator;
    private final VaultLocationFactory vaultLocationFactory;
    private final VaultDocumentStorage vaultDocumentStorage;
    private final CompressionConfigurationProperties compressionConfigurationProperties;

    public void recompress(final DocumentEntity documentEntity, final DocumentCompression documentCompression) {
        log.info("Migrating archived document {} from {} compression to {}.", documentEntity.getId(),
                documentEntity.getCompression(), compressionConfigurationProperties.getAlgorithm());

        try (InputStream documentContentInputStream = vaultDocumentManager.readDocument(documentEntity)
                .getInputStream()) {
            final byte[] documentContent = documentContentInputStream.readAllBytes();

            vaultDocumentManager.removeDocument(documentEntity)
                    .doOnNext(processedEntity -> {
                        final VaultLocation vaultLocation = vaultLocationFactory.getLocation(documentEntity);

                        vaultDocumentStorage.persistDocument(processedEntity, documentContent, vaultLocation);
                    })
                    .subscribe();

            documentManipulator.updateCompression(documentEntity.getId(), documentCompression).subscribe();
        } catch (final IOException e) {
            throw new StorageAccessException("Unable to load document " + documentEntity.getId() + "!", e);
        }
    }
}
