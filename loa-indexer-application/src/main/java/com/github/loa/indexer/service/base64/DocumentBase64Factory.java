package com.github.loa.indexer.service.base64;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.vault.service.VaultLocationFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

/**
 * This service is responsible for creating base64 strings from documents.
 */
@Service
@RequiredArgsConstructor
public class DocumentBase64Factory {

    private final VaultLocationFactory vaultLocationFactory;

    public String getBase64String(final DocumentEntity documentEntity) {
        final File originalFile = vaultLocationFactory.getLocation(documentEntity);

        try {
            return Base64.getEncoder().encodeToString(Files.readAllBytes(originalFile.toPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
