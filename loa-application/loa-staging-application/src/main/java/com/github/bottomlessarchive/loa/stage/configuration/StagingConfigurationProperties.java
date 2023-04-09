package com.github.bottomlessarchive.loa.stage.configuration;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.nio.file.Path;

/**
 * Contains the configuration properties for the stage feature.
 *
 * @param location the location where the staged documents should be saved to
 */
@Validated
@ConfigurationProperties("loa.staging")
public record StagingConfigurationProperties(

        @NotNull
        Path location
) {
}
