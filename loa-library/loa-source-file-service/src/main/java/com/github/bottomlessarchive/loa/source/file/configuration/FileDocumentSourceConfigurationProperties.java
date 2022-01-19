package com.github.bottomlessarchive.loa.source.file.configuration;

import com.github.bottomlessarchive.loa.source.file.service.domain.FileEncodingType;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Contains the configurations that could be set from the property files regarding the file based document location
 * source processing.
 *
 * @param location  The location of the input file on the disc.
 * @param encoding  The encoding of the file.
 * @param skipLines How many lines should be skipped before starting to process locations again.
 */
@ConfigurationProperties("loa.source.file")
public record FileDocumentSourceConfigurationProperties(

        String location,
        FileEncodingType encoding,
        long skipLines
) {
}
