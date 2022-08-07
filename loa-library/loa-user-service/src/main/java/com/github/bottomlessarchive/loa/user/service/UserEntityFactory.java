package com.github.bottomlessarchive.loa.user.service;

import com.github.bottomlessarchive.loa.user.repository.UserRepository;
import com.github.bottomlessarchive.loa.user.repository.domain.UserDatabaseEntity;
import com.github.bottomlessarchive.loa.user.service.domain.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserEntityFactory {

    private final UserRepository userRepository;
    private final UserEntityTransformer userEntityTransformer;

    public Optional<UserEntity> getUserEntity(final UUID userId) {
        return userRepository.getUser(userId)
                .map(userEntityTransformer::transform);
    }

    public Optional<UserEntity> getUserEntity(final String name, final String password) {
        return userRepository.getUser(name, password)
                .map(userEntityTransformer::transform);
    }

    public UserEntity newUserEntity(final String name, final String password) {
        final UserDatabaseEntity userDatabaseEntity = new UserDatabaseEntity();

        userDatabaseEntity.setId(UUID.randomUUID());
        userDatabaseEntity.setName(name);
        userDatabaseEntity.setPassword(password);

        userRepository.insertUser(userDatabaseEntity);

        return userEntityTransformer.transform(userDatabaseEntity);
    }
}
