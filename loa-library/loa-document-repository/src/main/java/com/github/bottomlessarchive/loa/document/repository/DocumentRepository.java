package com.github.bottomlessarchive.loa.document.repository;

import com.github.bottomlessarchive.loa.document.repository.domain.DocumentDatabaseEntity;
import com.github.bottomlessarchive.loa.document.repository.domain.DocumentStatusAggregateEntity;
import com.github.bottomlessarchive.loa.document.repository.domain.DocumentTypeAggregateEntity;
import com.github.bottomlessarchive.loa.repository.configuration.RepositoryConfigurationProperties;
import com.github.bottomlessarchive.loa.repository.service.HexConverter;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import lombok.RequiredArgsConstructor;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.mongodb.client.model.Accumulators.sum;
import static com.mongodb.client.model.Aggregates.group;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

@Component
@RequiredArgsConstructor
public class DocumentRepository {

    private final HexConverter hexConverter;
    private final RepositoryConfigurationProperties repositoryConfigurationProperties;
    private final MongoCollection<DocumentDatabaseEntity> documentDatabaseEntityMongoCollection;

    public void insertDocument(final DocumentDatabaseEntity documentDatabaseEntity) {
        documentDatabaseEntityMongoCollection.insertOne(documentDatabaseEntity);
    }

    public Optional<DocumentDatabaseEntity> findByChecksumAndFileSizeAndType(final byte[] checksum, final long fileSize,
            final String type) {
        return Optional.ofNullable(documentDatabaseEntityMongoCollection.find(and(eq("checksum", checksum),
                eq("fileSize", fileSize), eq("type", type))).first());
    }

    public void addSourceLocation(final UUID documentId, final String documentLocationId) {
        documentDatabaseEntityMongoCollection.updateOne(eq("_id", documentId), Updates.addToSet("sourceLocations",
                hexConverter.decode(documentLocationId)));
    }

    public Optional<DocumentDatabaseEntity> findById(final UUID documentId) {
        return Optional.ofNullable(documentDatabaseEntityMongoCollection.find(eq("_id", documentId)).first());
    }

    public void removeDocument(final UUID documentId) {
        documentDatabaseEntityMongoCollection.deleteOne(eq("_id", documentId));
    }

    public void updateCompression(final UUID documentId, final String compression) {
        documentDatabaseEntityMongoCollection.updateOne(eq("_id", documentId), set("compression", compression));
    }

    /**
     * Update the status of the documents available in the repository to the provided value.
     *
     * @param status the value to update the status of the documents to
     */
    public void updateStatus(final String status) {
        documentDatabaseEntityMongoCollection.updateMany(Filters.empty(), set("status", status));
    }

    public void updateStatus(final UUID documentId, final String status) {
        documentDatabaseEntityMongoCollection.updateOne(eq("_id", documentId), set("status", status));
    }

    /**
     * Returns a {@link Iterable} that will emit all the documents that have the provided status in the database.
     *
     * @return every element in the database
     */
    public Iterable<DocumentDatabaseEntity> findByStatus(final String status) {
        return documentDatabaseEntityMongoCollection.find(eq("status", status))
                // We don't want to have the cursor closed while processing. It would be a better idea to set
                // a maximum timeout in milliseconds but that only configurable on the server side.
                .noCursorTimeout(repositoryConfigurationProperties.noCursorTimeout());
    }

    /**
     * Returns an {@link Iterable} that will emit all the documents that are available in the database.
     *
     * @return every element in the database
     */
    public Iterable<DocumentDatabaseEntity> findAll() {
        return documentDatabaseEntityMongoCollection.find()
                // We don't want to have the cursor closed while processing. It would be a better idea to set
                // a maximum timeout in milliseconds but that only configurable on the server side.
                .noCursorTimeout(repositoryConfigurationProperties.noCursorTimeout());
    }

    public long estimateCount() {
        return documentDatabaseEntityMongoCollection.estimatedDocumentCount();
    }

    public Map<String, Integer> countByType() {
        final List<Bson> countByStatusAggregate = Collections.singletonList(group("$type", sum("count", 1L)));

        return StreamSupport.stream(documentDatabaseEntityMongoCollection.aggregate(countByStatusAggregate,
                        DocumentTypeAggregateEntity.class).spliterator(), false)
                .collect(Collectors.toMap(DocumentTypeAggregateEntity::getId, DocumentTypeAggregateEntity::getCount));
    }

    public Map<String, Integer> countByStatus() {
        final List<Bson> countByStatusAggregate = Collections.singletonList(group("$status", sum("count", 1L)));

        return StreamSupport.stream(documentDatabaseEntityMongoCollection.aggregate(countByStatusAggregate,
                        DocumentStatusAggregateEntity.class).spliterator(), false)
                .collect(Collectors.toMap(DocumentStatusAggregateEntity::getId, DocumentStatusAggregateEntity::getCount));
    }
}
