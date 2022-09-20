package com.github.bottomlessarchive.loa.io.service.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("loa.http")
public record HttpClientConfigurationProperties(
        int parallelism
) {
}
