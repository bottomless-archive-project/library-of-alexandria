package com.github.bottomlessarchive.loa.vault.service.location.configuration;

import com.github.bottomlessarchive.loa.vault.domain.VaultType;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Holds the configuration for the vault location.
 *
 * @param type The type of the vault.
 */
@Validated
@ConfigurationProperties("loa.vault.location")
public record VaultLocationConfigurationProperties(

        @NotNull
        VaultType type
) {
}
