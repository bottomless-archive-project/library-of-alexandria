package com.github.bottomlessarchive.loa.user.repository.configuration;

import com.github.bottomlessarchive.loa.user.repository.domain.UserDatabaseEntity;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserMongoConfiguration {

    @Bean
    public MongoCollection<UserDatabaseEntity> userDatabaseEntityMongoCollection(final MongoDatabase mongoDatabase) {
        final MongoCollection<UserDatabaseEntity> userDatabaseEntityMongoCollection =
                mongoDatabase.getCollection("user", UserDatabaseEntity.class);

        final IndexOptions uniqueUserIndexOptions = new IndexOptions()
                .name("unique_user")
                .unique(true);

        userDatabaseEntityMongoCollection
                .createIndex(Indexes.ascending("name"), uniqueUserIndexOptions);

        return userDatabaseEntityMongoCollection;
    }
}
