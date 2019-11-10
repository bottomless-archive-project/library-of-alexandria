package com.github.loa.vault.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("loa.vault")
public class VaultConfigurationProperties {

    private int versionNumber;
}
