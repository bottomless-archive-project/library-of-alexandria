package com.github.loa.administrator;

import com.github.loa.administrator.command.compressor.SilentCompressorConfigurationProperties;
import com.github.loa.compression.configuration.CompressionConfigurationProperties;
import com.github.loa.repository.configuration.RepositoryConfigurationProperties;
import com.github.loa.vault.client.configuration.VaultClientConfigurationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * This application contains useful commands that manipulates either the database or the vault. These commands are
 * usually batch processing in nature and fixes/corrects errors, remove certain documents etc.
 */
@EnableConfigurationProperties({SilentCompressorConfigurationProperties.class, RepositoryConfigurationProperties.class,
        VaultClientConfigurationProperties.class, CompressionConfigurationProperties.class})
@SpringBootApplication(scanBasePackages = "com.github.loa")
public class LibraryAdministratorApplication {

    public static void main(final String[] args) {
        SpringApplication.run(LibraryAdministratorApplication.class, args);
    }
}
