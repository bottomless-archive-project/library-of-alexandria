package com.github.loa.document.repository.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("loa.database")
public class DatabaseConfigurationProperties {

    private String host;
    private int port;
    private String username;
    private String password;
}
