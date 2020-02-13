package com.github.loa.document.repository.configuration;

import com.github.loa.document.repository.domain.DocumentDatabaseEntity;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Configuration
public class MongoConfiguration {

    @Bean
    public MongoCollection<DocumentDatabaseEntity> documentDatabaseEntityMongoCollection(
            final MongoDatabase mongoDatabase) {
        final MongoCollection<DocumentDatabaseEntity> documentDatabaseEntityMongoCollection =
                mongoDatabase.getCollection("document", DocumentDatabaseEntity.class);

        final IndexOptions statusIndexOptions = new IndexOptions();
        statusIndexOptions.name("status_index");
        Mono.from(documentDatabaseEntityMongoCollection.createIndex(Indexes.ascending("status"),
                statusIndexOptions)).subscribe();

        final IndexOptions uniqueFileIndexOptions = new IndexOptions();
        uniqueFileIndexOptions.name("unique_file");
        uniqueFileIndexOptions.unique(true);
        Mono.from(documentDatabaseEntityMongoCollection.createIndex(Indexes.ascending(
                "checksum", "fileSize", "type"), uniqueFileIndexOptions)).subscribe();

        return documentDatabaseEntityMongoCollection;
    }

    @Bean
    public MongoDatabase mongoDatabase() {
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        MongoClientSettings settings = MongoClientSettings.builder()
                .codecRegistry(pojoCodecRegistry)
                .build();
        MongoClient mongoClient = MongoClients.create(settings);

        return mongoClient.getDatabase("loa");
    }
}
