package com.github.bottomlessarchive.loa.stage.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("loa.staging")
public record StageConfigurationProperties(

        String location
) {
}
