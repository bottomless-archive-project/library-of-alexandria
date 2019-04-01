package com.github.loa.vault.service.location.smb;

import com.github.loa.vault.domain.exception.VaultAccessException;
import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2CreateOptions;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.EnumSet;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "loa.vault.location.type", havingValue = "smb")
public class SMBFileManipulator {

    //TODO: Add configuration
    private final SMBClient smbClient;

    public void writeFile(final String location, final File file) {
        try (Connection connection = smbClient.connect("SERVERNAME")) {
            final AuthenticationContext ac = new AuthenticationContext("USERNAME", "PASSWORD".toCharArray(), "DOMAIN");
            final Session session = connection.authenticate(ac);

            // Connect to Share
            try (DiskShare share = (DiskShare) session.connectShare("SHARENAME")) {
                final com.hierynomus.smbj.share.File openFile = share.openFile(location,
                        EnumSet.of(AccessMask.GENERIC_WRITE),
                        EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL),
                        EnumSet.of(SMB2ShareAccess.FILE_SHARE_WRITE),
                        SMB2CreateDisposition.FILE_CREATE,
                        EnumSet.noneOf(SMB2CreateOptions.class)
                );

                openFile.write(Files.readAllBytes(file.toPath()), 0);
            }

            file.delete();
        } catch (IOException e) {
            throw new VaultAccessException("Unable to move file to vault!", e);
        }
    }

    public byte[] readFile(final String location) {
        try (Connection connection = smbClient.connect("SERVERNAME")) {
            final AuthenticationContext ac = new AuthenticationContext("USERNAME", "PASSWORD".toCharArray(), "DOMAIN");
            final Session session = connection.authenticate(ac);

            // Connect to Share
            try (DiskShare share = (DiskShare) session.connectShare("SHARENAME")) {
                try (final com.hierynomus.smbj.share.File openFile = share.openFile(location,
                        EnumSet.of(AccessMask.GENERIC_WRITE),
                        EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL),
                        EnumSet.of(SMB2ShareAccess.FILE_SHARE_WRITE),
                        SMB2CreateDisposition.FILE_CREATE,
                        EnumSet.noneOf(SMB2CreateOptions.class))) {
                    return openFile.getInputStream().readAllBytes();
                }
            }
        } catch (IOException e) {
            throw new VaultAccessException("Unable to move file to vault!", e);
        }
    }
}
