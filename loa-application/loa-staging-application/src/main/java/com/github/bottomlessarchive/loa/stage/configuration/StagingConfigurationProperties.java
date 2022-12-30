package com.github.bottomlessarchive.loa.stage.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

/**
 * Contains the configuration properties for the stage feature.
 *
 * @param location the location where the staged documents should be saved to
 */
@ConfigurationProperties("loa.staging")
public record StagingConfigurationProperties(
        Path location
) {
}
