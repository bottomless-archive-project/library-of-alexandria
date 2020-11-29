package com.github.loa.indexer;

import com.github.loa.compression.configuration.CompressionConfigurationProperties;
import com.github.loa.indexer.configuration.IndexerConfigurationProperties;
import com.github.loa.indexer.service.configuration.IndexDatabaseConfigurationProperties;
import com.github.loa.repository.configuration.RepositoryConfigurationProperties;
import com.github.loa.vault.client.configuration.VaultClientConfigurationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties({RepositoryConfigurationProperties.class, VaultClientConfigurationProperties.class,
        CompressionConfigurationProperties.class, IndexDatabaseConfigurationProperties.class, IndexerConfigurationProperties.class})
@SpringBootApplication(scanBasePackages = "com.github.loa")
public class LibraryIndexerApplication {

    public static void main(final String[] args) {
        SpringApplication.run(LibraryIndexerApplication.class, args);
    }
}
