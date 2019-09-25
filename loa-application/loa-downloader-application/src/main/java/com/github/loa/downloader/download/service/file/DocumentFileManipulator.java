package com.github.loa.downloader.download.service.file;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.domain.DocumentType;
import com.github.loa.downloader.download.service.file.domain.FailedToArchiveException;
import com.github.loa.stage.service.StageLocationFactory;
import com.github.loa.vault.client.service.VaultClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * This service is responsible for the manipulating of document files. For example moving them to the vault or removing
 * them if necessary.
 */
@Service
@RequiredArgsConstructor
public class DocumentFileManipulator {

    private final StageLocationFactory stageLocationFactory;
    private final VaultClientService vaultClientService;

    /**
     * Move a document's file from the staging area to the vault. The document's metadata is not updated by this method!
     *
     * @param documentEntity the document's id that we want to move to the vault
     */
    public void moveToVault(final DocumentEntity documentEntity) {
        try (final InputStream documentContents = new FileInputStream(
                stageLocationFactory.getLocation(documentEntity))) {
            vaultClientService.archiveDocument(documentEntity, documentContents);
        } catch (Exception e) {
            throw new FailedToArchiveException("Unable to move document to vault!", e);
        }

        cleanup(documentEntity);
    }

    /**
     * Clean up after a document's download by removing all the staging information belonging to that document.
     *
     * @param documentEntity the document to clean up after
     */
    public void cleanup(final DocumentEntity documentEntity) {
        cleanup(documentEntity.getId(), documentEntity.getType());
    }

    public void cleanup(final String documentId, final DocumentType documentType) {
        final File stageFileLocation = stageLocationFactory.getLocation(documentId, documentType);

        stageFileLocation.delete();
    }
}
