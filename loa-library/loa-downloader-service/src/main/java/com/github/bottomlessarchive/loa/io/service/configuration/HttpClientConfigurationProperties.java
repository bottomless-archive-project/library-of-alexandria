package com.github.bottomlessarchive.loa.io.service.configuration;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("loa.http")
public record HttpClientConfigurationProperties(

        @Min(1)
        int parallelism
) {
}
