package com.github.bottomlessarchive.loa.beacon.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("loa.beacon")
public record BeaconConfigurationProperties(
        String storagePath
) {
}
