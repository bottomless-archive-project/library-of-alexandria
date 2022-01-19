package com.github.bottomlessarchive.loa.compression.configuration;

import com.github.bottomlessarchive.loa.compression.domain.DocumentCompression;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Contains the configuration properties for the compression.
 */
@ConfigurationProperties("loa.compression")
public record CompressionConfigurationProperties(
        DocumentCompression algorithm
) {
}
