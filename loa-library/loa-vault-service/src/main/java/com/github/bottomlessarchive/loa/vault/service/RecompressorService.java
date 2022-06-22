package com.github.bottomlessarchive.loa.vault.service;

import com.github.bottomlessarchive.loa.compression.domain.DocumentCompression;
import com.github.bottomlessarchive.loa.document.service.DocumentManipulator;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.vault.domain.exception.StorageAccessException;
import com.github.bottomlessarchive.loa.vault.service.backend.service.VaultDocumentStorage;
import com.github.bottomlessarchive.loa.vault.service.location.VaultLocation;
import com.github.bottomlessarchive.loa.vault.service.location.VaultLocationFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
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

    //TODO: This can be optimized. We shouldn't read the whole document into memory!
    public void recompress(final DocumentEntity documentEntity, final DocumentCompression documentCompression) {
        if (log.isInfoEnabled()) {
            log.info("Migrating archived document {} from {} compression to {}.", documentEntity.getId(),
                    documentEntity.getCompression(), documentCompression);
        }

        try (InputStream documentContentInputStream = vaultDocumentManager.readDocument(documentEntity)
                .getInputStream()) {
            final byte[] documentContent = documentContentInputStream.readAllBytes();

            vaultDocumentManager.removeDocument(documentEntity);

            final VaultLocation vaultLocation = vaultLocationFactory.getLocation(documentEntity, documentCompression);

            vaultDocumentStorage.persistDocument(documentEntity, new ByteArrayInputStream(documentContent), vaultLocation,
                    documentContent.length);

            documentManipulator.updateCompression(documentEntity.getId(), documentCompression);
        } catch (final IOException e) {
            throw new StorageAccessException("Unable to load document " + documentEntity.getId() + "!", e);
        }
    }
}
