package com.github.bottomlessarchive.loa.batch.repository.configuration;

import com.github.bottomlessarchive.loa.batch.repository.domain.BatchDatabaseEntity;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BatchMongoConfiguration {

    @Bean
    public MongoCollection<BatchDatabaseEntity> batchDatabaseEntityMongoCollection(final MongoDatabase mongoDatabase) {
        final MongoCollection<BatchDatabaseEntity> batchDatabaseEntityMongoCollection =
                mongoDatabase.getCollection("batch", BatchDatabaseEntity.class);

        final IndexOptions statusIndexOptions = new IndexOptions()
                .name("status_index");

        batchDatabaseEntityMongoCollection
                .createIndex(Indexes.ascending("status"), statusIndexOptions);

        return batchDatabaseEntityMongoCollection;
    }
}
