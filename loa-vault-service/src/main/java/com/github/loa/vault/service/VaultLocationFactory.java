package com.github.loa.vault.service;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.vault.configuration.VaultConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * A factory that create a {@link java.io.File} location for the document.
 */
@Service
@RequiredArgsConstructor
public class VaultLocationFactory {

    private final VaultConfiguration vaultConfiguration;

    /**
     * Create the location for a given {@link DocumentEntity}.
     *
     * @param documentEntity the entity to create the location for
     * @return the location of the document
     */
    public File getLocation(final DocumentEntity documentEntity) {
        return getLocation(documentEntity.getId());
    }

    /**
     * Create the location for a given document id.
     *
     * @param documentId the id of the entity to create the location for
     * @return the location of the document
     */
    public File getLocation(final String documentId) {
        return new File(vaultConfiguration.getLocation(), documentId + ".pdf");
    }
}
