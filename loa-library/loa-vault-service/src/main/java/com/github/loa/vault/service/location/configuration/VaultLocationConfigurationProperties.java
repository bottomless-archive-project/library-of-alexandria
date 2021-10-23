package com.github.loa.vault.service.location.configuration;

import com.github.loa.vault.domain.VaultType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

/**
 * A {@link ConfigurationProperties} class that contains the configuration of the vault at runtime.
 */
@Getter
@ConstructorBinding
@RequiredArgsConstructor
@ConfigurationProperties("loa.vault.location")
public class VaultLocationConfigurationProperties {

    /**
     * The type of the vault.
     */
    private final VaultType type;
}
