package com.github.loa.vault.client.configuration;

import lombok.Data;

@Data
public class VaultClientLocationConfigurationProperties {

    private String host;
    private int port;

    public String getLocation() {
        return "http://" + host + ":" + port;
    }
}
