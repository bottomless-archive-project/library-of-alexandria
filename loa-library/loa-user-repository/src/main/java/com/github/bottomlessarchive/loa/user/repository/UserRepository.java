package com.github.bottomlessarchive.loa.user.repository;

import com.github.bottomlessarchive.loa.user.repository.domain.UserDatabaseEntity;
import com.mongodb.client.MongoCollection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

@Component
@RequiredArgsConstructor
public class UserRepository {

    private final MongoCollection<UserDatabaseEntity> userDatabaseEntityMongoCollection;

    public void insertUser(final UserDatabaseEntity userDatabaseEntity) {
        userDatabaseEntityMongoCollection.insertOne(userDatabaseEntity);
    }

    public Optional<UserDatabaseEntity> getUser(final UUID userId) {
        return Optional.ofNullable(userDatabaseEntityMongoCollection.find(eq("_id", userId)).first());
    }

    public Optional<UserDatabaseEntity> getUser(final String name, final String password) {
        return Optional.ofNullable(userDatabaseEntityMongoCollection.find(
                and(eq("name", name), eq("password", password))).first());
    }
}
