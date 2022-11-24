package com.github.bottomlessarchive.loa.downloader.service.source.beacon.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("loa.downloader.beacon")
public record BeaconDownloaderConfigurationProperties(

        //TODO: Support more than one beacons!
        String activeBeacon,
        int requestSize
) {
}
