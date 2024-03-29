package com.github.bottomlessarchive.loa.administrator.command.user;

import com.github.bottomlessarchive.loa.administrator.command.user.configuration.RegisterUserConfigurationProperties;
import com.github.bottomlessarchive.loa.user.service.UserEntityFactory;
import com.github.bottomlessarchive.loa.user.service.domain.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "loa.command", havingValue = "register-user")
public class RegisterUserCommand implements CommandLineRunner {

    private final UserEntityFactory userEntityFactory;
    private final RegisterUserConfigurationProperties registerUserConfigurationProperties;

    @Override
    public void run(final String... args) {
        if (!registerUserConfigurationProperties.isNameValid()) {
            throw new IllegalArgumentException("The provided username is invalid! It should be at least 3 characters long!"
                    + " Please set the loa.command.register-user.name property to a valid value.");
        }

        if (!registerUserConfigurationProperties.isPasswordValid()) {
            throw new IllegalArgumentException("The provided password is invalid! It should be at least 3 characters long!"
                    + " Please set the loa.command.register-user.password property to a valid value.");
        }

        log.info("Creating new user with name: {}.", registerUserConfigurationProperties.name());

        final UserEntity userEntity = userEntityFactory.newUserEntity(registerUserConfigurationProperties.name(),
                registerUserConfigurationProperties.password());

        log.info("The new user was created with id: {}!", userEntity.getId());
    }
}
