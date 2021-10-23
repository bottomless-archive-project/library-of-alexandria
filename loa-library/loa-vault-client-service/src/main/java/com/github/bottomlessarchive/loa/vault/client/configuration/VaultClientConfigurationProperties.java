package com.github.bottomlessarchive.loa.vault.client.configuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.util.Map;

@Getter
@ConstructorBinding
@RequiredArgsConstructor
@ConfigurationProperties("loa.vault.client")
public class VaultClientConfigurationProperties {

    private final Map<String, VaultClientLocationConfigurationProperties> locations;

    public VaultClientLocationConfigurationProperties getLocation(final String vaultName) {
        return locations.get(vaultName);
    }
}
