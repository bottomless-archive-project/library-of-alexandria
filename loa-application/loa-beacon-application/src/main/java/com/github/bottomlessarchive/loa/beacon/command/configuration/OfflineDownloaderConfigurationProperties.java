package com.github.bottomlessarchive.loa.beacon.command.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.nio.file.Path;

@ConditionalOnProperty("loa.beacon.offline")
public record OfflineDownloaderConfigurationProperties(

        Path sourceFile,
        long skip
) {
}
