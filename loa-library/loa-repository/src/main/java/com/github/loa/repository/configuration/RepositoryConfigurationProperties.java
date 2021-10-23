package com.github.loa.repository.configuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@Getter
@ConstructorBinding
@RequiredArgsConstructor
@ConfigurationProperties("loa.database")
public class RepositoryConfigurationProperties {

    private final String host;
    private final int port;
    private final boolean noCursorTimeout;
    private final String uri;

    public boolean isUriConfiguration() {
        return uri != null && !uri.isEmpty();
    }
}
