package com.github.bottomlessarchive.loa.vault.service.location.configuration;

import com.github.bottomlessarchive.loa.vault.domain.VaultType;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Holds the configuration for the vault location.
 *
 * @param type The type of the vault.
 */
@ConfigurationProperties("loa.vault.location")
public record VaultLocationConfigurationProperties(
        VaultType type
) {
}
