package com.github.bottomlessarchive.loa.beacon.command.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

@ConfigurationProperties("loa.beacon.offline")
public record OfflineDownloaderConfigurationProperties(

        Path sourceFile,
        long skip
) {
}
