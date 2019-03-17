package com.github.loa.downloader.command.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Contains the configuration for the thread executor responsible for the downloading tasks.
 */
@Data
@Component
@ConfigurationProperties("loa.downloader.executor")
public class DownloaderExecutorConfiguration {

    private int threadCount;
    private int queueLength;
}
