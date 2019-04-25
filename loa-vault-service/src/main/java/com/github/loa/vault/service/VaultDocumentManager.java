package com.github.loa.vault.service;

import com.github.loa.compression.service.CompressionService;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.vault.domain.exception.VaultAccessException;
import com.github.loa.vault.service.location.domain.VaultLocation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class VaultDocumentManager {

    private final VaultLocationFactory vaultLocationFactory;
    private final CompressionService compressionService;

    public void archiveDocument(final String documentId, final InputStream documentContents) {
        try (final VaultLocation vaultLocation = vaultLocationFactory.getLocation(documentId)) {
            compressionService.compress(documentContents, vaultLocation.destination());
        } catch (IOException e) {
            throw new VaultAccessException("Unable to move document with id " + documentId + " to the vault!", e);
        }
    }

    public byte[] readDocument(final DocumentEntity documentEntity) {
        return readDocument(documentEntity.getId());
    }

    public byte[] readDocument(final String documentId) {
        try (final VaultLocation vaultLocation = vaultLocationFactory.getLocation(documentId)) {
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            compressionService.decompress(vaultLocation.content(), outputStream);

            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new VaultAccessException("Unable to read document with id " + documentId + " from the vault!", e);
        }
    }
}
