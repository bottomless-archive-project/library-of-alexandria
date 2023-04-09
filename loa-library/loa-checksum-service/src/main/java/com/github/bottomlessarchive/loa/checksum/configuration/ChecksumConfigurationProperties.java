package com.github.bottomlessarchive.loa.checksum.configuration;

import com.github.bottomlessarchive.loa.checksum.domain.ChecksumType;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Contains the configuration properties for the checksum.
 */
@Validated
@ConfigurationProperties("loa.checksum")
public record ChecksumConfigurationProperties(

        @NotNull
        ChecksumType type
) {
}
