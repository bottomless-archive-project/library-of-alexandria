package com.github.bottomlessarchive.loa.conductor.configuration;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("loa.indexer.database")
public record IndexDatabaseConfigurationProperties(

        @NotBlank
        String host,

        @Min(1)
        @Max(65535)
        int port
) {
}
