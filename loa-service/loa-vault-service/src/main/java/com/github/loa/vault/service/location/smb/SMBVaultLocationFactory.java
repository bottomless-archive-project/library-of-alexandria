package com.github.loa.vault.service.location.smb;

import com.github.loa.compression.configuration.CompressionConfigurationProperties;
import com.github.loa.compression.domain.DocumentCompression;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.vault.service.VaultLocationFactory;
import com.github.loa.vault.service.location.domain.VaultLocation;
import com.github.loa.vault.service.location.smb.domain.SMBVaultLocation;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "loa.vault.location.type", havingValue = "smb")
public class SMBVaultLocationFactory implements VaultLocationFactory {

    private final SMBFileManipulator smbFileManipulator;
    private final CompressionConfigurationProperties compressionConfigurationProperties;

    @Override
    public VaultLocation getLocation(final DocumentEntity documentEntity) {
        return getLocation(documentEntity, compressionConfigurationProperties.getAlgorithm());
    }

    @Override
    public VaultLocation getLocation(final DocumentEntity documentEntity, final DocumentCompression compression) {
        return new SMBVaultLocation(documentEntity.getId() + "." + documentEntity.getType().getFileExtension()
                + "." + compression.getFileExtension(), smbFileManipulator);
    }
}
