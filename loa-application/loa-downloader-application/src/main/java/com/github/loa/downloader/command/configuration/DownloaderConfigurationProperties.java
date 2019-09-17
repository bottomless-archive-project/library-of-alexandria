package com.github.loa.downloader.command.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("loa.downloader")
public class DownloaderConfigurationProperties {

    /**
     * 1 = M1 - RC-2 -> Updated because normal files had a dot at the end.
     * 2 = RC-3+
     */
    private int versionNumber;
}
