package com.github.bottomlessarchive.loa.administrator.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

@ConfigurationProperties("loa.administrator")
public record AdministratorConfigurationProperties(

        Path stagingDirectory
) {
}
