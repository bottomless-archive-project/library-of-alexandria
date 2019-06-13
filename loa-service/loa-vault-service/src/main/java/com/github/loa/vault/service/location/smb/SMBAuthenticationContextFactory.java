package com.github.loa.vault.service.location.smb;

import com.github.loa.vault.configuration.location.smb.SMBConfigurationProperties;
import com.hierynomus.smbj.auth.AuthenticationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SMBAuthenticationContextFactory {

    private final SMBConfigurationProperties smbConfigurationProperties;

    /**
     * Return a new authentication context based on the provided configuration properties.
     *
     * @return the new authentication context
     */
    public AuthenticationContext newContext() {
        final String username = smbConfigurationProperties.getUsername() == null ? ""
                : smbConfigurationProperties.getUsername();
        final char[] password = smbConfigurationProperties.getPassword() == null ? new char[]{}
                : smbConfigurationProperties.getPassword().toCharArray();

        return new AuthenticationContext(username, password, smbConfigurationProperties.getDomain());
    }
}
