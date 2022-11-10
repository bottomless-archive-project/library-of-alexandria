package com.github.bottomlessarchive.loa.user.service.domain;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class UserEntity {

    private final UUID id;
    private final String name;
    private final String password;
}
