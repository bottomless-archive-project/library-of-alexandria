package com.github.loa.downloader;

import com.github.loa.downloader.configuration.DownloaderConfigurationProperties;
import com.github.loa.queue.configuration.QueueConfigurationProperties;
import com.github.loa.repository.configuration.RepositoryConfigurationProperties;
import com.github.loa.source.configuration.DocumentSourceConfiguration;
import com.github.loa.stage.configuration.StageConfigurationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.reactive.function.client.ClientHttpConnectorAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * The runner class of the downloader application.
 */
@EnableScheduling
@EnableConfigurationProperties({QueueConfigurationProperties.class, DownloaderConfigurationProperties.class,
        DocumentSourceConfiguration.class, StageConfigurationProperties.class,
        RepositoryConfigurationProperties.class})
@SpringBootApplication(scanBasePackages = "com.github.loa", exclude = ClientHttpConnectorAutoConfiguration.class)
public class LibraryDownloaderApplication {

    public static void main(final String[] args) {
        SpringApplication.run(LibraryDownloaderApplication.class, args);
    }
}
