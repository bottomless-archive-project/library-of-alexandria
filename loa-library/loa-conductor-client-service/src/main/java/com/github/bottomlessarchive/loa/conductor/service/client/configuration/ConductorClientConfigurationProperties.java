package com.github.bottomlessarchive.loa.conductor.service.client.configuration;

import com.github.bottomlessarchive.loa.application.domain.ApplicationType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("loa.conductor")
public record ConductorClientConfigurationProperties(

        @NotNull
        String host,

        @Min(1)
        @Max(65535)
        int port,

        @NotNull
        ApplicationType applicationType,

        @Min(0)
        @Max(65535)
        int applicationPort
) {

    public String getUrl() {
        return "http://" + host + ":" + port;
    }
}
