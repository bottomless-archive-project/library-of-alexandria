package com.github.bottomlessarchive.loa.compression.configuration;

import com.github.bottomlessarchive.loa.compression.domain.DocumentCompression;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Contains the configuration properties for the compression.
 */
@Validated
@ConfigurationProperties("loa.compression")
public record CompressionConfigurationProperties(

        @NotNull
        DocumentCompression algorithm
) {
}
