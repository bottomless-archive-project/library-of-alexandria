package com.github.loa.document.repository;

import com.github.loa.document.repository.domain.DocumentDatabaseEntity;
import com.mongodb.reactivestreams.client.MongoCollection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

@Component
@RequiredArgsConstructor
public class DocumentRepository {

    private final MongoCollection<DocumentDatabaseEntity> documentDatabaseEntityMongoCollection;

    public Mono<DocumentDatabaseEntity> insertDocument(final DocumentDatabaseEntity documentDatabaseEntity) {
        return Mono.from(documentDatabaseEntityMongoCollection.insertOne(documentDatabaseEntity))
                .thenReturn(documentDatabaseEntity);
    }

    public Mono<DocumentDatabaseEntity> findById(final UUID documentId) {
        return Mono.from(documentDatabaseEntityMongoCollection.find(eq("_id", documentId)));
    }

    public Flux<DocumentDatabaseEntity> findByStatus(final String status) {
        return Flux.from(documentDatabaseEntityMongoCollection.find(eq("status", status)));
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

    public Flux<DocumentDatabaseEntity> findAll() {
        return Flux.from(documentDatabaseEntityMongoCollection.find());
    }

    public Mono<Long> count() {
        return Mono.from(documentDatabaseEntityMongoCollection.countDocuments());
    }
}
