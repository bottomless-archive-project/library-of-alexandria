package com.github.bottomlessarchive.loa.vault;

import com.github.bottomlessarchive.loa.vault.service.listener.VaultQueueListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * This application is responsible for storing and retrieving the documents.
 * <p>
 * The main logic of the archiving is found in the {@link VaultQueueListener}.
 */
@SpringBootApplication(scanBasePackages = "com.github.loa")
@ConfigurationPropertiesScan(basePackages = "com.github.loa")
public class LibraryVaultApplication {

    public static void main(final String[] args) {
        SpringApplication.run(LibraryVaultApplication.class, args);
    }
}
