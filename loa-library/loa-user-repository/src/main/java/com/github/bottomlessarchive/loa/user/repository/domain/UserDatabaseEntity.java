package com.github.bottomlessarchive.loa.user.repository.domain;

import lombok.Getter;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonId;

import java.util.UUID;

@Getter
@Setter
public class UserDatabaseEntity {

    @BsonId
    private UUID id;
    private String name;
    private String password;
}
