package com.github.bottomlessarchive.loa.conductor.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("loa.indexer.database")
public record IndexDatabaseConfigurationProperties(
        String host,
        int port
) {
}
