package com.github.loa.source.file.configuration;

import com.github.loa.source.file.service.domain.FileEncodingType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Contains the configurations that could be set from the property files regarding the file based document location
 * source processing.
 */
@Data
@Component
@ConfigurationProperties("loa.source.file")
public class FileDocumentSourceConfigurationProperties {

    /**
     * The location of the input file on the disc.
     */
    private String location;

    /**
     * The encoding of the file.
     *
     * @see FileEncodingType
     */
    private FileEncodingType encoding;

    /**
     * How many lines should be skipped before starting to process locations again.
     */
    private int skipLines;
}
