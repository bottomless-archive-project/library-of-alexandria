package com.github.loa.repository.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("loa.database")
public class RepositoryConfigurationProperties {

    private String host;
    private int port;
}
