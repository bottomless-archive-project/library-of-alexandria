package com.github.loa.statistics.repository.configuration;

import com.github.loa.statistics.repository.domain.StatisticsDatabaseEntity;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class StatisticsMongoConfiguration {

    @Bean
    public MongoCollection<StatisticsDatabaseEntity> statisticsDatabaseEntityMongoCollection(final MongoDatabase mongoDatabase) {
        final MongoCollection<StatisticsDatabaseEntity> documentDatabaseEntityMongoCollection =
                mongoDatabase.getCollection("statistics", StatisticsDatabaseEntity.class);

        final IndexOptions createdAtIndexOptions = new IndexOptions()
                .name("created_at_index");

        Mono.from(documentDatabaseEntityMongoCollection
                .createIndex(Indexes.ascending("created_at"), createdAtIndexOptions))
                .subscribe();

        return documentDatabaseEntityMongoCollection;
    }
}
