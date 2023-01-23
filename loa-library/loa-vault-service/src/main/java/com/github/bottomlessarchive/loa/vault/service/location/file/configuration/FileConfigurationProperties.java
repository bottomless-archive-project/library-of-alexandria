package com.github.bottomlessarchive.loa.vault.service.location.file.configuration;

import com.github.bottomlessarchive.loa.vault.domain.VaultType;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

/**
 * A {@link ConfigurationProperties} class that contains the configuration of the vault location if the vault type
 * property is {@link VaultType#FILE} at runtime.
 *
 * @param path The path to the vault on the local filesystem.
 */
@ConfigurationProperties("loa.vault.location.file")
public record FileConfigurationProperties(

        Path path
) {
}
