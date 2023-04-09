package com.github.bottomlessarchive.loa.indexer.configuration;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("loa.indexer")
public record IndexerConfigurationProperties(

        @Min(1)
        int batchSize,

        @Min(1)
        int parallelism
) {
}
