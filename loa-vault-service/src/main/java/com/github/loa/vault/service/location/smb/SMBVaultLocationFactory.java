package com.github.loa.vault.service.location.smb;

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

    @Override
    public VaultLocation getLocation(String documentId) {
        return new SMBVaultLocation(documentId + ".pdf", smbFileManipulator);
    }

    @Override
    public VaultLocation getLocation(DocumentEntity documentEntity) {
        return getLocation(documentEntity.getId());
    }
}
