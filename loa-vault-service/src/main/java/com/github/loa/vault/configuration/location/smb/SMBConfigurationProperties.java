package com.github.loa.vault.configuration.location.smb;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * A {@link ConfigurationProperties} class that contains the SMB configuration of the vault location if the vault type
 * property is {@link com.github.loa.vault.domain.VaultType#SMB} at runtime.
 */
@Data
@Component
@ConfigurationProperties("loa.vault.location.smb")
public class SMBConfigurationProperties {

    /**
     * The name of the SMB server. Used to log in.
     */
    private String serverName;

    /**
     * The share name that used as the vault on the server.
     */
    private String shareName;

    /**
     * The path to the files on the share.
     */
    private String sharePath;

    /**
     * The username to authenticate with.
     */
    private String username;

    /**
     * The password to authenticate with.
     */
    private String password;

    /**
     * The domain to authenticate with.
     */
    private String domain;
}
