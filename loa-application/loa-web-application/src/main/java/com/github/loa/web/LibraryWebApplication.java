package com.github.loa.web;

import com.github.loa.compression.configuration.CompressionConfigurationProperties;
import com.github.loa.indexer.service.configuration.IndexDatabaseConfigurationProperties;
import com.github.loa.queue.configuration.QueueConfigurationProperties;
import com.github.loa.repository.configuration.RepositoryConfigurationProperties;
import com.github.loa.statistics.configuration.StatisticsConfigurationProperties;
import com.github.loa.vault.client.configuration.VaultClientConfigurationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableConfigurationProperties({RepositoryConfigurationProperties.class, VaultClientConfigurationProperties.class,
        QueueConfigurationProperties.class, CompressionConfigurationProperties.class, IndexDatabaseConfigurationProperties.class,
        StatisticsConfigurationProperties.class})
@SpringBootApplication(scanBasePackages = "com.github.loa")
public class LibraryWebApplication {

    public static void main(final String[] args) {
        SpringApplication.run(LibraryWebApplication.class, args);
    }
}
