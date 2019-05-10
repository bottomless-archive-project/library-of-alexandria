package com.github.loa.vault.configuration.location.file;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * A {@link ConfigurationProperties} class that contains the configuration of the vault location if the vault type
 * property is {@link com.github.loa.vault.domain.VaultType#FILE} at runtime.
 */
@Data
@Component
@ConfigurationProperties("loa.vault.location.file")
public class FileConfigurationProperties {

    /**
     * The path to the vault on the local filesystem.
     */
    private String path;
}
