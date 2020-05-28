package com.github.loa.vault.service.location.file;

import com.github.loa.compression.domain.DocumentCompression;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.vault.service.location.file.configuration.FileConfigurationProperties;
import com.github.loa.vault.service.location.VaultLocation;
import com.github.loa.vault.service.location.VaultLocationFactory;
import com.github.loa.vault.service.location.file.domain.FileVaultLocation;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * A factory that create a local disc backed {@link VaultLocation} instances for documents.
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "loa.vault.location.type", havingValue = "file", matchIfMissing = true)
public class FileVaultLocationFactory implements VaultLocationFactory {

    private final FileFactory fileFactory;
    private final FileConfigurationProperties fileConfigurationProperties;

    /**
     * Create the location for a given {@link DocumentEntity}.
     *
     * @param documentEntity the entity to create the location for
     * @return the location of the document
     */
    @Override
    public VaultLocation getLocation(final DocumentEntity documentEntity) {
        return getLocation(documentEntity, documentEntity.getCompression());
    }

    /**
     * Create the location for a given {@link DocumentEntity}. The filename part of the location is calculated using
     * the provided compression.
     *
     * @param documentEntity the entity of the document to return the location for
     * @param compression    the compression used in the location calculation
     * @return the location of the document
     */
    @Override
    public VaultLocation getLocation(final DocumentEntity documentEntity, final DocumentCompression compression) {
        if (compression == DocumentCompression.NONE) {
            return new FileVaultLocation(fileFactory.newFile(fileConfigurationProperties.getPath(),
                    documentEntity.getId() + "." + documentEntity.getType().getFileExtension()));
        } else {
            return new FileVaultLocation(fileFactory.newFile(fileConfigurationProperties.getPath(),
                    documentEntity.getId() + "." + documentEntity.getType().getFileExtension() + "."
                            + compression.getFileExtension()));
        }
    }
}
