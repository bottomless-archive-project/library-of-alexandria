package com.github.bottomlessarchive.loa.conductor.service.client.configuration;

import com.github.bottomlessarchive.loa.application.domain.ApplicationType;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("loa.conductor")
public record ConductorClientConfigurationProperties(
        String host,
        int port,
        ApplicationType applicationType,
        int applicationPort
) {

    public String getUrl() {
        return "http://" + host + ":" + port;
    }
}
