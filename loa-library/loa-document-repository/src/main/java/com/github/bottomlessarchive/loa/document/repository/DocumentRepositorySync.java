package com.github.bottomlessarchive.loa.document.repository;

import com.github.bottomlessarchive.loa.document.repository.domain.DocumentDatabaseEntity;
import com.github.bottomlessarchive.loa.repository.service.HexConverter;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Updates;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

@Component
@RequiredArgsConstructor
public class DocumentRepositorySync {

    private final HexConverter hexConverter;
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

    public void updateStatus(final UUID documentId, final String status) {
        documentDatabaseEntityMongoCollection.updateOne(eq("_id", documentId), set("status", status));
    }
}
