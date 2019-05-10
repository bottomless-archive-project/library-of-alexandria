package com.github.loa.indexer.service.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("loa.indexer.database")
public class DatabaseConfigurationProperties {

    private String host;
    private int port;
}
