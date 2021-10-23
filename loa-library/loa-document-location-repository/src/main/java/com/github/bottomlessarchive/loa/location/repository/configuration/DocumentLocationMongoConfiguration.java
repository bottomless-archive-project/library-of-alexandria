package com.github.bottomlessarchive.loa.location.repository.configuration;

import com.github.bottomlessarchive.loa.location.repository.domain.DocumentLocationDatabaseEntity;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DocumentLocationMongoConfiguration {

    @Bean
    public MongoCollection<DocumentLocationDatabaseEntity> documentLocationDatabaseEntityMongoCollection(
            final MongoDatabase mongoDatabase) {
        return mongoDatabase.getCollection("documentLocation", DocumentLocationDatabaseEntity.class);
    }
}
