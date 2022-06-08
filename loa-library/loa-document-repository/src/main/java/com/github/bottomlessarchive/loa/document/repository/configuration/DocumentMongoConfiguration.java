package com.github.bottomlessarchive.loa.document.repository.configuration;

import com.github.bottomlessarchive.loa.document.repository.domain.DocumentDatabaseEntity;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DocumentMongoConfiguration {

    @Bean
    public MongoCollection<DocumentDatabaseEntity> documentDatabaseEntityMongoCollection(final MongoDatabase mongoDatabase) {
        final MongoCollection<DocumentDatabaseEntity> documentDatabaseEntityMongoCollection =
                mongoDatabase.getCollection("document", DocumentDatabaseEntity.class);

        final IndexOptions statusIndexOptions = new IndexOptions()
                .name("status_index");

        documentDatabaseEntityMongoCollection
                .createIndex(Indexes.ascending("status"), statusIndexOptions);

        final IndexOptions typeIndexOptions = new IndexOptions()
                .name("type_index");

        documentDatabaseEntityMongoCollection
                .createIndex(Indexes.ascending("type"), typeIndexOptions);

        final IndexOptions uniqueFileIndexOptions = new IndexOptions()
                .name("unique_file")
                .unique(true);

        documentDatabaseEntityMongoCollection
                .createIndex(Indexes.ascending("checksum", "fileSize", "type"), uniqueFileIndexOptions);

        return documentDatabaseEntityMongoCollection;
    }
}
