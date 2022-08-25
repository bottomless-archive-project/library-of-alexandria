package com.github.bottomlessarchive.loa.user.repository;

import com.github.bottomlessarchive.loa.repository.document.Error;
import com.github.bottomlessarchive.loa.user.repository.domain.UserAlreadyExistsInDatabaseException;
import com.github.bottomlessarchive.loa.user.repository.domain.UserDatabaseEntity;
import com.mongodb.MongoWriteException;
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
        try {
            userDatabaseEntityMongoCollection.insertOne(userDatabaseEntity);
        } catch (final MongoWriteException e) {
            if (Error.DUPLICATE.hasErrorCode(e.getCode())) {
                throw new UserAlreadyExistsInDatabaseException("User " + userDatabaseEntity.getName()
                        + " already exists in the database!");
            }

            throw e;
        }
    }

    public Optional<UserDatabaseEntity> getUser(final UUID userId) {
        return Optional.ofNullable(userDatabaseEntityMongoCollection.find(eq("_id", userId)).first());
    }

    public Optional<UserDatabaseEntity> getUser(final String name, final String password) {
        return Optional.ofNullable(userDatabaseEntityMongoCollection.find(
                and(eq("name", name), eq("password", password))).first());
    }
}
