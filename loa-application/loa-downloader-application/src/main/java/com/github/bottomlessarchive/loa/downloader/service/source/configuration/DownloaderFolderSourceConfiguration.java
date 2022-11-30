package com.github.bottomlessarchive.loa.downloader.service.source.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("loa.downloader.source.folder")
public record DownloaderFolderSourceConfiguration(

        String location,
        boolean shouldRemove
) {
}
