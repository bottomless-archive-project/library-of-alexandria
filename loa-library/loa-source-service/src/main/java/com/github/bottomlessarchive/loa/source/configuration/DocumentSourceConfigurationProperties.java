package com.github.bottomlessarchive.loa.source.configuration;

import com.github.bottomlessarchive.loa.source.domain.DocumentSourceType;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Contains the configurations that could be set from the property files regarding the document location source processing.
 *
 * @param name The name of the source. Will be inserted for every document and document location that crawled from the provided source.
 *             The default value is unknown.
 * @param type The type of the source to use when generating new document locations.
 */
@Validated
@ConfigurationProperties("loa.source")
public record DocumentSourceConfigurationProperties(

        @NotNull
        String name,

        @NotNull
        DocumentSourceType type
) {
}
