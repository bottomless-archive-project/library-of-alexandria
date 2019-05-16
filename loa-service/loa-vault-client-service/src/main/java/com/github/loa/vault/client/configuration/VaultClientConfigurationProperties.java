package com.github.loa.vault.client.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("loa.vault.client")
public class VaultClientConfigurationProperties {

    private String host;
    private int port;
}
