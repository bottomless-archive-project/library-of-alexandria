package com.github.bottomlessarchive.loa.document.repository.configuration;

import com.github.bottomlessarchive.loa.document.repository.domain.DocumentDatabaseEntity;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class DocumentMongoConfiguration {

    @Bean
    public MongoCollection<DocumentDatabaseEntity> documentDatabaseEntityMongoCollection(final MongoDatabase mongoDatabase) {
        final MongoCollection<DocumentDatabaseEntity> documentDatabaseEntityMongoCollection =
                mongoDatabase.getCollection("document", DocumentDatabaseEntity.class);

        final IndexOptions statusIndexOptions = new IndexOptions()
                .name("status_index");

        Mono.from(documentDatabaseEntityMongoCollection
                .createIndex(Indexes.ascending("status"), statusIndexOptions))
                .subscribe();

        final IndexOptions uniqueFileIndexOptions = new IndexOptions()
                .name("unique_file")
                .unique(true);

        Mono.from(documentDatabaseEntityMongoCollection
                .createIndex(Indexes.ascending("checksum", "fileSize", "type"), uniqueFileIndexOptions))
                .subscribe();

        return documentDatabaseEntityMongoCollection;
    }
}
