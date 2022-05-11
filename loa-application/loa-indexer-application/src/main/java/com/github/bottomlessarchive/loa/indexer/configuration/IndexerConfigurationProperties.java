package com.github.bottomlessarchive.loa.indexer.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("loa.indexer")
public record IndexerConfigurationProperties(
        int parallelism
) {
}
