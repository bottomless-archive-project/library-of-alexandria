package com.github.bottomlessarchive.loa.downloader.service.source.beacon.loadocloader.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

@ConfigurationProperties("loa.downloader.loadoc")
public record LoadocLoaderBeaconConfigurationProperties(

        Path location
) {
}
