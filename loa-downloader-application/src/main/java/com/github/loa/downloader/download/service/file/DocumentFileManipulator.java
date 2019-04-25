package com.github.loa.downloader.download.service.file;

import com.github.loa.stage.service.StageLocationFactory;
import com.github.loa.vault.service.VaultDocumentManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * This service is responsible for the manipulating of document files. For example moving them to the vault or removing
 * them if necessary.
 */
@Service
@RequiredArgsConstructor
public class DocumentFileManipulator {

    private final StageLocationFactory stageLocationFactory;
    private final VaultDocumentManager vaultDocumentManager;

    /**
     * Move a document's file from the staging area to the vault. The document's metadata is not updated by this method!
     *
     * @param documentId the document's id that we want to move to the vault
     */
    public void moveToVault(final String documentId) {
        try {
            vaultDocumentManager.archiveDocument(documentId, new FileInputStream(stageLocationFactory.getLocation(documentId)));

            cleanup(documentId);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Unable to move document to vault!", e);
        }
    }

    /**
     * Clean up after a document's download by removing all the staging information belonging to that document.
     *
     * @param documentId the document to clean up after
     */
    public void cleanup(final String documentId) {
        final File stageFileLocation = stageLocationFactory.getLocation(documentId);

        stageFileLocation.delete();
    }
}
