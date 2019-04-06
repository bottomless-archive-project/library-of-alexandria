package com.github.loa.vault.service.location.smb.domain;

import com.github.loa.vault.service.location.domain.VaultLocation;
import com.github.loa.vault.service.location.smb.SMBFileManipulator;
import lombok.RequiredArgsConstructor;

import java.io.File;

@RequiredArgsConstructor
public class SMBVaultLocation implements VaultLocation {

    private final String location;
    private final SMBFileManipulator smbFileManipulator;

    @Override
    public void move(final File file) {
        smbFileManipulator.writeFile(location, file);

        file.delete();
    }

    @Override
    public byte[] getContent() {
        return smbFileManipulator.readFile(location);
    }
}
