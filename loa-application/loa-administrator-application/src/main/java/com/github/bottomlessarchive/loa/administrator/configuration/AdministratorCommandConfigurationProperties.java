package com.github.bottomlessarchive.loa.administrator.configuration;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("loa.command")
public record AdministratorCommandConfigurationProperties(

        @NotBlank
        String name
) {
}
