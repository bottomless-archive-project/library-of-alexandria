package com.github.bottomlessarchive.loa.downloader.service.source.beacon.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

@ConfigurationProperties("loa.downloader.beacon")
public record BeaconDownloaderConfigurationProperties(

        String name,
        String host,
        int port,
        int requestSize,
        Path location //TODO: This should be loader.location in a different prop config
) {
}
