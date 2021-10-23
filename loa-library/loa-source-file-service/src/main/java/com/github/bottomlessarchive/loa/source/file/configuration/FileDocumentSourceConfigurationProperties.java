package com.github.bottomlessarchive.loa.source.file.configuration;

import com.github.bottomlessarchive.loa.source.file.service.domain.FileEncodingType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

/**
 * Contains the configurations that could be set from the property files regarding the file based document location
 * source processing.
 */
@Getter
@ConstructorBinding
@RequiredArgsConstructor
@ConfigurationProperties("loa.source.file")
public class FileDocumentSourceConfigurationProperties {

    /**
     * The location of the input file on the disc.
     */
    private final String location;

    /**
     * The encoding of the file.
     *
     * @see FileEncodingType
     */
    private final FileEncodingType encoding;

    /**
     * How many lines should be skipped before starting to process locations again.
     */
    private final long skipLines;
}
