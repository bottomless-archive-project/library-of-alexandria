package com.github.loa.source.configuration;

import com.github.loa.source.domain.DocumentSourceType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Contains the configurations that could be set from the property files regarding the document location source processing.
 */
@Data
@Component
@ConfigurationProperties("loa.source")
public class DocumentSourceConfiguration {

    /**
     * The name of the source. Will be inserted for every document and document location that crawled from the provided source.
     * The default value is unknown.
     */
    private String name;

    /**
     * The type of the source to use when generating new document locations.
     */
    private DocumentSourceType type;
}
