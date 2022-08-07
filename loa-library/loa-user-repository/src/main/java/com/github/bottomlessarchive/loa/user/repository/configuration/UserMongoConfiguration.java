package com.github.bottomlessarchive.loa.user.repository.configuration;

import com.github.bottomlessarchive.loa.user.repository.domain.UserDatabaseEntity;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserMongoConfiguration {

    @Bean
    public MongoCollection<UserDatabaseEntity> userDatabaseEntityMongoCollection(final MongoDatabase mongoDatabase) {
        return mongoDatabase.getCollection("user", UserDatabaseEntity.class);
    }
}
