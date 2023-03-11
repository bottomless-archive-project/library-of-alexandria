package com.github.bottomlessarchive.loa.beacon.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

@ConfigurationProperties("loa.beacon")
public record BeaconConfigurationProperties(

        Path storageDirectory,
        Path stagingDirectory
) {
}
