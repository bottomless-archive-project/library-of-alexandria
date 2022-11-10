package com.github.bottomlessarchive.loa.user.service;

import com.github.bottomlessarchive.loa.user.repository.domain.UserDatabaseEntity;
import com.github.bottomlessarchive.loa.user.service.domain.UserEntity;
import org.springframework.stereotype.Service;

@Service
public class UserEntityTransformer {

    public UserEntity transform(final UserDatabaseEntity userDatabaseEntity) {
        return UserEntity.builder()
                .id(userDatabaseEntity.getId())
                .name(userDatabaseEntity.getName())
                .password(userDatabaseEntity.getPassword())
                .build();
    }
}
