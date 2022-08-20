package com.github.bottomlessarchive.loa.administrator.command.user.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("loa.command.register-user")
public record RegisterUserConfigurationProperties(
        String name,
        String password
) {

    public boolean isNameValid() {
        return name != null && name.length() > 3;
    }

    public boolean isPasswordValid() {
        return password != null && password.length() > 3;
    }
}
