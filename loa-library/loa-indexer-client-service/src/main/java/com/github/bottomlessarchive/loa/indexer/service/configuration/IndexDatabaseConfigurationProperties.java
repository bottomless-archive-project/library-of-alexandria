package com.github.bottomlessarchive.loa.indexer.service.configuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@Getter
@ConstructorBinding
@RequiredArgsConstructor
@ConfigurationProperties("loa.indexer.database")
public class IndexDatabaseConfigurationProperties {

    private final String host;
    private final int port;
    private final boolean enabled;
}
