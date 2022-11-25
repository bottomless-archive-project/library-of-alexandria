package com.github.bottomlessarchive.loa.downloader.service.source.configuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties("loa.downloader.source.folder")
public class DownloaderFolderSourceConfiguration {

    private final String location;
    private final boolean shouldRemove;
}
