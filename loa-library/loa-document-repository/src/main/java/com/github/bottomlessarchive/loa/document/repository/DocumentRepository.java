package com.github.bottomlessarchive.loa.document.repository;

import com.github.bottomlessarchive.loa.document.repository.domain.DocumentDatabaseEntity;
import com.github.bottomlessarchive.loa.document.repository.domain.DocumentStatusAggregateEntity;
import com.github.bottomlessarchive.loa.document.repository.domain.DocumentTypeAggregateEntity;
import com.github.bottomlessarchive.loa.repository.configuration.RepositoryConfigurationProperties;
import com.github.bottomlessarchive.loa.repository.service.HexConverter;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.reactivestreams.client.MongoCollection;
import lombok.RequiredArgsConstructor;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    public Mono<DocumentDatabaseEntity> insertDocument(final DocumentDatabaseEntity documentDatabaseEntity) {
        return Mono.from(documentDatabaseEntityMongoCollection.insertOne(documentDatabaseEntity))
                .thenReturn(documentDatabaseEntity);
    }

    public Mono<Void> removeDocument(final UUID documentId) {
        return Mono.from(documentDatabaseEntityMongoCollection.deleteOne(eq("_id", documentId)))
                .then();
    }

    public Mono<DocumentDatabaseEntity> findById(final UUID documentId) {
        return Mono.from(documentDatabaseEntityMongoCollection.find(eq("_id", documentId)));
    }

    public Mono<DocumentDatabaseEntity> findByChecksumAndFileSizeAndType(final byte[] checksum, final long fileSize, final String type) {
        return Mono.from(documentDatabaseEntityMongoCollection.find(and(eq("checksum", checksum),
                eq("fileSize", fileSize), eq("type", type))));
    }

    /**
     * Returns a {@link Flux} that will emit all the documents that have the provided status in the database.
     *
     * @return a flux emitting every elements in the database
     */
    public Flux<DocumentDatabaseEntity> findByStatus(final String status) {
        return Flux.from(
                documentDatabaseEntityMongoCollection.find(eq("status", status))
                        // We don't want to have the cursor closed while processing. It would be a better idea to set
                        // a maximum timeout in milliseconds but that only configurable on the server side.
                        .noCursorTimeout(repositoryConfigurationProperties.noCursorTimeout())
        );
    }

    /**
     * Returns a {@link Flux} that will emit all the documents that are available in the database.
     *
     * @return a flux emitting every elements in the database
     */
    public Flux<DocumentDatabaseEntity> findAll() {
        return Flux.from(
                documentDatabaseEntityMongoCollection.find()
                        // We don't want to have the cursor closed while processing. It would be a better idea to set
                        // a maximum timeout in milliseconds but that only configurable on the server side.
                        .noCursorTimeout(repositoryConfigurationProperties.noCursorTimeout())
        );
    }

    /**
     * Update the status of the documents available in the repository to the provided value.
     *
     * @param status the value to update the status of the documents to
     * @return the result of the update
     */
    public Mono<Void> updateStatus(final String status) {
        return Mono.from(documentDatabaseEntityMongoCollection
                        .updateMany(Filters.empty(), set("status", status)))
                .then();
    }

    public Mono<Void> updateStatus(final UUID documentId, final String status) {
        return Mono.from(documentDatabaseEntityMongoCollection
                        .updateOne(eq("_id", documentId), set("status", status)))
                .then();
    }

    public Mono<Void> updateCompression(final UUID documentId, final String compression) {
        return Mono.from(documentDatabaseEntityMongoCollection
                        .updateOne(eq("_id", documentId), set("compression", compression)))
                .then();
    }

    public Mono<Void> addSourceLocation(final UUID documentId, final String documentLocationId) {
        return Mono.from(documentDatabaseEntityMongoCollection
                        .updateOne(eq("_id", documentId), Updates.addToSet("sourceLocations", hexConverter.decode(documentLocationId))))
                .then();
    }

    public Mono<Long> count() {
        return Mono.from(documentDatabaseEntityMongoCollection.countDocuments());
    }

    public Mono<Long> estimateCount() {
        return Mono.from(documentDatabaseEntityMongoCollection.estimatedDocumentCount());
    }

    public Mono<Map<String, Integer>> countByStatus() {
        final List<Bson> countByStatusAggregate = Collections.singletonList(group("$status", sum("count", 1L)));

        return Flux.from(documentDatabaseEntityMongoCollection.aggregate(countByStatusAggregate, DocumentStatusAggregateEntity.class))
                .collectMap(DocumentStatusAggregateEntity::getId, DocumentStatusAggregateEntity::getCount);
    }

    public Mono<Map<String, Integer>> countByType() {
        final List<Bson> countByStatusAggregate = Collections.singletonList(group("$type", sum("count", 1L)));

        return Flux.from(documentDatabaseEntityMongoCollection.aggregate(countByStatusAggregate, DocumentTypeAggregateEntity.class))
                .collectMap(DocumentTypeAggregateEntity::getId, DocumentTypeAggregateEntity::getCount);
    }
}
