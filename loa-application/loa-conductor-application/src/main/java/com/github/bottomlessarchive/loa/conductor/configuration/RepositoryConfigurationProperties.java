package com.github.bottomlessarchive.loa.conductor.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("loa.database")
public record RepositoryConfigurationProperties(

        String host,
        int port,
        boolean noCursorTimeout,
        String uri
) {

    public boolean isUriConfiguration() {
        return uri != null && !uri.isEmpty();
    }
}
