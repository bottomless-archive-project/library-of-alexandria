package com.github.loa.vault.service;

import com.github.loa.vault.configuration.VaultConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * A factory that creates final saving {@link java.io.File} location instances.
 */
@Service
@RequiredArgsConstructor
public class DocumentLocationFactory {

    private final VaultConfiguration vaultConfiguration;

    public File newLocation(final String documentId) {
        return new File(vaultConfiguration.getLocation(), documentId + ".pdf");
    }
}
