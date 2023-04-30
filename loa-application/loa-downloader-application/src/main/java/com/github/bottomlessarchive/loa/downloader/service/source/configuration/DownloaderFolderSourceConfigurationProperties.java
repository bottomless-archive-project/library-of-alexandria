package com.github.bottomlessarchive.loa.downloader.service.source.configuration;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("loa.downloader.source.folder")
public record DownloaderFolderSourceConfigurationProperties(

        String location,
        boolean shouldRemove,

        @NotNull
        String sourceName
) {
}
