package com.github.bottomlessarchive.loa.vault.service.location.sqlite.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

/**
 * Configuration properties for the SQLite vault location backend.
 *
 * @param path      The path to the directory containing the SQLite vault database files.
 * @param batchSize The maximum number of documents stored per SQLite database file before rotating to a new one.
 */
@ConfigurationProperties("loa.vault.location.sqlite")
public record SqliteConfigurationProperties(

        Path path,
        int batchSize
) {

    public SqliteConfigurationProperties {
        if (batchSize <= 0) {
            batchSize = 100000;
        }
    }
}
