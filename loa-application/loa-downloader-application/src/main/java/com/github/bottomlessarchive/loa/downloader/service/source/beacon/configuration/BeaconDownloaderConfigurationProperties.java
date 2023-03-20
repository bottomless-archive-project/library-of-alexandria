package com.github.bottomlessarchive.loa.downloader.service.source.beacon.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

@ConfigurationProperties("loa.downloader.beacon")
public record BeaconDownloaderConfigurationProperties(

        String activeBeacon,
        int requestSize,
        Path location
) {
}
