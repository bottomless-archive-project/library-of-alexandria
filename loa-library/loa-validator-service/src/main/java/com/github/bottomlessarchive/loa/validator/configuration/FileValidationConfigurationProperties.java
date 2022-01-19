package com.github.bottomlessarchive.loa.validator.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param maximumArchiveSize Maximum file size to archive in bytes. Documents bigger than this in size are not archived.
 */
@ConfigurationProperties("loa.validation")
public record FileValidationConfigurationProperties(
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
