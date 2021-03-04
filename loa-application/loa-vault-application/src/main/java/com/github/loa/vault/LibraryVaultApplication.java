package com.github.loa.vault;

import com.github.loa.checksum.configuration.ChecksumConfigurationProperties;
import com.github.loa.compression.configuration.CompressionConfigurationProperties;
import com.github.loa.queue.configuration.QueueConfigurationProperties;
import com.github.loa.repository.configuration.RepositoryConfigurationProperties;
import com.github.loa.vault.configuration.VaultConfigurationProperties;
import com.github.loa.vault.service.location.configuration.VaultLocationConfigurationProperties;
import com.github.loa.vault.service.location.file.configuration.FileConfigurationProperties;
import com.github.loa.vault.service.location.s3.configuration.S3ConfigurationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * This application is responsible for storing and retrieving the documents.
 * <p>
 * The main logic of the archiving is found in the {@link com.github.loa.vault.service.listener.VaultQueueListener}.
 */
@EnableConfigurationProperties({VaultConfigurationProperties.class, ChecksumConfigurationProperties.class,
        RepositoryConfigurationProperties.class, CompressionConfigurationProperties.class, QueueConfigurationProperties.class,
        FileConfigurationProperties.class, S3ConfigurationProperties.class, VaultLocationConfigurationProperties.class})
@SpringBootApplication(scanBasePackages = "com.github.loa")
public class LibraryVaultApplication {

    public static void main(final String[] args) {
        SpringApplication.run(LibraryVaultApplication.class, args);
    }
}
