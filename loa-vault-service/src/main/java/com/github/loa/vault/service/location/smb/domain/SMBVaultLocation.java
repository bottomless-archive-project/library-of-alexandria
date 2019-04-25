package com.github.loa.vault.service.location.smb.domain;

import com.github.loa.vault.service.location.domain.VaultLocation;
import com.github.loa.vault.service.location.smb.SMBFileManipulator;
import lombok.RequiredArgsConstructor;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

@RequiredArgsConstructor
public class SMBVaultLocation implements VaultLocation {

    private final String location;
    private final SMBFileManipulator smbFileManipulator;
    private final ByteArrayOutputStream destination = new ByteArrayOutputStream();

    @Override
    public OutputStream destination() {
        return destination;
    }

    @Override
    public InputStream content() {
        return smbFileManipulator.readFile(location);
    }

    @Override
    public void close() {
        smbFileManipulator.writeFile(location, destination);
    }
}
