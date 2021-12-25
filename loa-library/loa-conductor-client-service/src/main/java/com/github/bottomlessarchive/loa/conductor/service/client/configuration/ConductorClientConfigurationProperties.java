package com.github.bottomlessarchive.loa.conductor.service.client.configuration;

import com.github.bottomlessarchive.loa.application.domain.ApplicationType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@Getter
@ConstructorBinding
@RequiredArgsConstructor
@ConfigurationProperties("loa.conductor")
public class ConductorClientConfigurationProperties {

    private final String host;
    private final int port;
    private final ApplicationType applicationType;

    public String getUrl() {
        return "http://" + host + ":" + port;
    }
}
