package com.github.bottomlessarchive.loa.validator.configuration;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * @param maximumArchiveSize Maximum file size to archive in bytes. Documents bigger than this in size are not archived.
 */
@Validated
@ConfigurationProperties("loa.validation")
public record FileValidationConfigurationProperties(

        @Min(0)
        long maximumArchiveSize
) {

    public FileValidationConfigurationProperties(final long maximumArchiveSize) {
        if (maximumArchiveSize == 0) {
            this.maximumArchiveSize = 8589934592L;
        } else {
            this.maximumArchiveSize = maximumArchiveSize;
        }
    }
}
