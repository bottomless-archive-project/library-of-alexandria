package com.github.loa.downloader.command.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("loa.downloader")
public class DownloaderConfiguration {

    private int versionNumber;
}
