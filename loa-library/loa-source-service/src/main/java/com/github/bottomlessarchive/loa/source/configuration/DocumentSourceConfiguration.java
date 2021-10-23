package com.github.bottomlessarchive.loa.source.configuration;

import com.github.bottomlessarchive.loa.source.domain.DocumentSourceType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

/**
 * Contains the configurations that could be set from the property files regarding the document location source processing.
 */
@Getter
@ConstructorBinding
@RequiredArgsConstructor
@ConfigurationProperties("loa.source")
public class DocumentSourceConfiguration {

    /**
     * The name of the source. Will be inserted for every document and document location that crawled from the provided source.
     * The default value is unknown.
     */
    private final String name;

    /**
     * The type of the source to use when generating new document locations.
     */
    private final DocumentSourceType type;
}
