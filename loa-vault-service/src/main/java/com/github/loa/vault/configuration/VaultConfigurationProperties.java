package com.github.loa.vault.configuration;

import com.github.loa.vault.domain.VaultType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * A {@link ConfigurationProperties} class that contains the configuration of the vault at runtime.
 */
@Data
@Component
@ConfigurationProperties("loa.vault.location")
public class VaultConfigurationProperties {

    /**
     * The type of the vault.
     */
    private VaultType type;
}
