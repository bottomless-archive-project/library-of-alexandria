package com.github.bottomlessarchive.loa.stage.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

@ConfigurationProperties("loa.stage")
public record StageConfigurationProperties(

        Path location
) {
}
