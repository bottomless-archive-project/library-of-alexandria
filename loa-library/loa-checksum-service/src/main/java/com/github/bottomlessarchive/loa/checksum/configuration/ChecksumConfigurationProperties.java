package com.github.bottomlessarchive.loa.checksum.configuration;

import com.github.bottomlessarchive.loa.checksum.domain.ChecksumType;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Contains the configuration properties for the checksum.
 */
@ConfigurationProperties("loa.checksum")
public record ChecksumConfigurationProperties(
        ChecksumType type
) {
}
