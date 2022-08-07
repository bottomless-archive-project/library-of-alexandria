package com.github.bottomlessarchive.loa.user.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("loa.user")
public record UserConfigurationProperties(

        boolean enabled
) {
}
