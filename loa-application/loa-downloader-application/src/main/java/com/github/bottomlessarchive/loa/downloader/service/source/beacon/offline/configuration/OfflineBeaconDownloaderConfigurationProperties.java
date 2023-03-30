package com.github.bottomlessarchive.loa.downloader.service.source.beacon.offline.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

@ConfigurationProperties("loa.downloader.beacon.offline")
public record OfflineBeaconDownloaderConfigurationProperties(

        long resultSize,
        Path resultLocation
) {
}
