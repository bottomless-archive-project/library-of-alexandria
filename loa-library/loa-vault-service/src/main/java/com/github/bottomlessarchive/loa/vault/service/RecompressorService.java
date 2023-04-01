package com.github.bottomlessarchive.loa.vault.service;

import com.github.bottomlessarchive.loa.compression.domain.DocumentCompression;
import com.github.bottomlessarchive.loa.compression.service.file.FileCompressionService;
import com.github.bottomlessarchive.loa.document.service.DocumentManipulator;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.stage.service.StageLocationFactory;
import com.github.bottomlessarchive.loa.stage.service.domain.StageLocation;
import com.github.bottomlessarchive.loa.vault.domain.exception.StorageAccessException;
import com.github.bottomlessarchive.loa.vault.service.backend.service.VaultDocumentStorage;
import com.github.bottomlessarchive.loa.vault.service.location.VaultLocation;
import com.github.bottomlessarchive.loa.vault.service.location.VaultLocationFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecompressorService {

    private final VaultDocumentManager vaultDocumentManager;
    private final DocumentManipulator documentManipulator;
    private final VaultLocationFactory vaultLocationFactory;
    private final VaultDocumentStorage vaultDocumentStorage;
    private final StageLocationFactory stageLocationFactory;
    private final FileCompressionService fileCompressionService;

    public void recompress(final DocumentEntity documentEntity, final DocumentCompression documentCompression) {
        if (log.isInfoEnabled()) {
            log.info("Migrating archived document {} from {} compression to {}.", documentEntity.getId(),
                    documentEntity.getCompression(), documentCompression);
        }

        if (documentEntity.getCompression().equals(documentCompression)) {
            return;
        }

        try (InputStream documentContentInputStream = vaultDocumentManager.readDocument(documentEntity)
                .getInputStream()) {

            try (StageLocation originalContent = stageLocationFactory.getLocation(UUID.randomUUID())) {
                // TODO: This can be optimized. We shouldn't read the whole document into memory! Stream it instead!
                // This might work, but needs testing:
                /*try (OutputStream outputStream = Files.newOutputStream(originalContent.getPath())) {
                    documentContentInputStream.transferTo(outputStream);
                }*/

                Files.write(originalContent.getPath(), documentContentInputStream.readAllBytes());

                final Path compressedFilePath = fileCompressionService.compressDocument(originalContent.getPath(), documentCompression);

                vaultDocumentManager.removeDocument(documentEntity);

                final VaultLocation vaultLocation = vaultLocationFactory.getLocation(documentEntity, documentCompression);

                vaultDocumentStorage.persistDocument(documentEntity, Files.newInputStream(compressedFilePath), vaultLocation,
                        Files.size(compressedFilePath));

                // In case when the compression target is NONE, then the path for the original content and the new content is the same
                // the file will be deleted by the originalContent's close call.
                if (!originalContent.getPath().equals(compressedFilePath)) {
                    Files.delete(compressedFilePath);
                }
            }

            documentManipulator.updateCompression(documentEntity.getId(), documentCompression);
        } catch (final IOException e) {
            throw new StorageAccessException("Unable to load document " + documentEntity.getId() + "!", e);
        }
    }
}
