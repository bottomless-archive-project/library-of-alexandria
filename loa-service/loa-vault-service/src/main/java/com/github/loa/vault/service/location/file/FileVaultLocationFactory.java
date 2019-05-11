package com.github.loa.vault.service.location.file;

import com.github.loa.compression.configuration.CompressionConfigurationProperties;
import com.github.loa.compression.domain.DocumentCompression;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.vault.configuration.location.file.FileConfigurationProperties;
import com.github.loa.vault.service.VaultLocationFactory;
import com.github.loa.vault.service.location.domain.VaultLocation;
import com.github.loa.vault.service.location.file.domain.FileVaultLocation;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * A factory that create a local disc backed {@link VaultLocation} instances for documents.
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "loa.vault.location.type", havingValue = "file")
public class FileVaultLocationFactory implements VaultLocationFactory {

    private final FileConfigurationProperties fileConfigurationProperties;
    private final CompressionConfigurationProperties compressionConfigurationProperties;

    /**
     * Create the location for a given {@link DocumentEntity}.
     *
     * @param documentEntity the entity to create the location for
     * @return the location of the document
     */
    public VaultLocation getLocation(final DocumentEntity documentEntity) {
        return getLocation(documentEntity, compressionConfigurationProperties.getAlgorithm());
    }

    public VaultLocation getLocation(final DocumentEntity documentEntity, final DocumentCompression compression) {
        return new FileVaultLocation(new File(fileConfigurationProperties.getPath(), documentEntity.getId() + "."
                + documentEntity.getType().getFileExtension() + "." + compression.getFileExtension()));
    }
}
