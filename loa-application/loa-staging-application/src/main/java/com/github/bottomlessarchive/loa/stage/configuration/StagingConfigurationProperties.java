package com.github.bottomlessarchive.loa.stage.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Contains the configuration properties for the stage feature.
 *
 * @param path the location where the staged documents should be saved to
 */
@ConfigurationProperties("loa.stage")
public record StagingConfigurationProperties(
        String path
) {
}
