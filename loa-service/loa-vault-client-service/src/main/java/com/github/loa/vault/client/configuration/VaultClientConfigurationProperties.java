package com.github.loa.vault.client.configuration;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Setter
@Component
@ConfigurationProperties("loa.vault.client")
public class VaultClientConfigurationProperties {

    private Map<String, VaultClientLocationConfigurationProperties> locations;

    public VaultClientLocationConfigurationProperties getLocation(final String vaultName) {
        return locations.get(vaultName);
    }
}
