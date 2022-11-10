package com.github.bottomlessarchive.loa.administrator.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("loa.command")
public record AdministratorCommandConfigurationProperties(

        String name
) {
}
