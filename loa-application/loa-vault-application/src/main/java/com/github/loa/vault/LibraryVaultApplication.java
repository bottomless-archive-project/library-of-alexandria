package com.github.loa.vault;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * This application is responsible for storing and retrieving the documents.
 * <p>
 * The main logic of the archiving is found in the {@link com.github.loa.vault.service.listener.VaultQueueListener}.
 */
@SpringBootApplication(scanBasePackages = "com.github.loa")
@ConfigurationPropertiesScan(basePackages = "com.github.loa")
public class LibraryVaultApplication {

    public static void main(final String[] args) {
        SpringApplication.run(LibraryVaultApplication.class, args);
    }
}
