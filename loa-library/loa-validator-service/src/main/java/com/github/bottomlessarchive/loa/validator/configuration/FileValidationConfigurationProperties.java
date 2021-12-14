package com.github.bottomlessarchive.loa.validator.configuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@Getter
@ConstructorBinding
@RequiredArgsConstructor
@ConfigurationProperties("loa.validation")
public class FileValidationConfigurationProperties {

    /**
     * Maximum file size to archive in bytes. Documents bigger than this in size are not archived.
     */
    private final long maximumArchiveSize = 8589934592L;
}
