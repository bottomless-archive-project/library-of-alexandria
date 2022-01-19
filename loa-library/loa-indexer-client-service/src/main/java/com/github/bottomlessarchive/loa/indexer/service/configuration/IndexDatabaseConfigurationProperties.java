package com.github.bottomlessarchive.loa.indexer.service.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("loa.indexer.database")
public record IndexDatabaseConfigurationProperties(
        String host,
        int port,
        boolean enabled
) {
}
