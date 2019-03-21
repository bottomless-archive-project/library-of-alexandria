package com.github.loa.downloader.target.service.document;

import com.github.loa.vault.configuration.VaultConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * A factory that creates {@link java.io.File} instances in the staging area. These files could be used to save the
 * document for a short time for pre-processing before moving it into the vault.
 */
//TODO: Move this file to a staging module
@Service
@RequiredArgsConstructor
public class DocumentStagingLocationFactory {

    //TODO: create a staging config and don't use vault's
    private final VaultConfiguration vaultConfiguration;

    /**
     * Return a file in the staging area that's uniquely generated for the provided document id.
     *
     * @param documentId the document's id that we need to create the location for
     * @return the location created in the staging area
     */
    public File newStagingLocation(final String documentId) {
        return new File(vaultConfiguration.getTemporaryLocation(), documentId + ".pdf");
    }
}
