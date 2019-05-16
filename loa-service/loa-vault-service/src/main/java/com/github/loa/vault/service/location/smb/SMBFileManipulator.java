package com.github.loa.vault.service.location.smb;

import com.github.loa.vault.configuration.location.smb.SMBConfigurationProperties;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;

/**
 * This service is responsible for connecting to an SMB share and manipulating files there. The connection is based on
 * the provided connection properties.
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "loa.vault.location.type", havingValue = "smb")
public class SMBFileManipulator {

    private final SMBClient smbClient;
    private final SMBConfigurationProperties smbConfigurationProperties;
    private final SMBAuthenticationContextFactory smbAuthenticationContextFactory;

    /**
     * Write a file to the SMB share.
     *
     * @param location         the file location to write on the share
     * @param documentContents the content to write
     */
    public void writeFile(final String location, final ByteArrayOutputStream documentContents) {
        try (Connection connection = smbClient.connect(smbConfigurationProperties.getHost())) {
            final AuthenticationContext authenticationContext = smbAuthenticationContextFactory.newContext();
            final Session session = connection.authenticate(authenticationContext);

            try (DiskShare share = (DiskShare) session.connectShare(smbConfigurationProperties.getShareName())) {
                final com.hierynomus.smbj.share.File openFile = share.openFile(location,
                        EnumSet.of(AccessMask.GENERIC_WRITE),
                        EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL),
                        EnumSet.of(SMB2ShareAccess.FILE_SHARE_WRITE),
                        SMB2CreateDisposition.FILE_CREATE,
                        EnumSet.noneOf(SMB2CreateOptions.class)
                );

                openFile.write(documentContents.toByteArray(), 0);
            }
        } catch (IOException e) {
            throw new VaultAccessException("Unable to move file to vault!", e);
        }
    }

    /**
     * Read a file on the SMB share.
     *
     * @param location the location of the file
     * @return the content of the file
     */
    public InputStream readFile(final String location) {
        try (Connection connection = smbClient.connect(smbConfigurationProperties.getHost())) {
            final AuthenticationContext authenticationContext = smbAuthenticationContextFactory.newContext();
            final Session session = connection.authenticate(authenticationContext);

            try (DiskShare share = (DiskShare) session.connectShare(smbConfigurationProperties.getShareName())) {
                try (final com.hierynomus.smbj.share.File openFile = share.openFile(location,
                        EnumSet.of(AccessMask.GENERIC_READ),
                        EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL),
                        EnumSet.of(SMB2ShareAccess.FILE_SHARE_READ),
                        SMB2CreateDisposition.FILE_OPEN,
                        EnumSet.noneOf(SMB2CreateOptions.class))) {
                    return openFile.getInputStream();
                }
            }
        } catch (IOException e) {
            throw new VaultAccessException("Unable to move file to vault!", e);
        }
    }

    public void removeFile(final String location) {
        try (Connection connection = smbClient.connect(smbConfigurationProperties.getHost())) {
            final AuthenticationContext authenticationContext = smbAuthenticationContextFactory.newContext();
            final Session session = connection.authenticate(authenticationContext);

            try (DiskShare share = (DiskShare) session.connectShare(smbConfigurationProperties.getShareName())) {
                share.rm(location);
            }
        } catch (IOException e) {
            throw new VaultAccessException("Unable to remove file from vault!", e);
        }
    }

    public long length(final String location) {
        try (Connection connection = smbClient.connect(smbConfigurationProperties.getHost())) {
            final AuthenticationContext authenticationContext = smbAuthenticationContextFactory.newContext();
            final Session session = connection.authenticate(authenticationContext);

            try (DiskShare share = (DiskShare) session.connectShare(smbConfigurationProperties.getShareName())) {
                return share.getFileInformation(location).getStandardInformation().getAllocationSize();
            }
        } catch (IOException e) {
            throw new VaultAccessException("Unable to remove file from vault!", e);
        }
    }
}
