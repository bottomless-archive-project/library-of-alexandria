package com.github.bottomlessarchive.loa.conductor.service.configuration;

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
}
