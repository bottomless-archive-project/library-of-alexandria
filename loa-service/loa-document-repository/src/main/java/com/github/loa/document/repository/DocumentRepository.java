package com.github.loa.document.repository;

import com.github.loa.document.repository.domain.DocumentDatabaseEntity;
import dev.morphia.Datastore;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.UpdateOperations;
import dev.morphia.query.internal.MorphiaCursor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DocumentRepository {

    private final Datastore datastore;

    public void insertDocument(final DocumentDatabaseEntity documentDatabaseEntity) {
        datastore.save(documentDatabaseEntity);
    }

    public DocumentDatabaseEntity findById(final String id) {
        return datastore.createQuery(DocumentDatabaseEntity.class)
                .field("_id").equal(id)
                .first();
    }

    public List<DocumentDatabaseEntity> findByStatus(final String status) {
        return datastore.createQuery(DocumentDatabaseEntity.class)
                .field("status").equal(status)
                .find(
                        new FindOptions()
                                .limit(100)
                )
                .toList();
    }

    public List<DocumentDatabaseEntity> findByCompression(final String compression) {
        return datastore.createQuery(DocumentDatabaseEntity.class)
                .field("compression").equal(compression)
                .find(
                        new FindOptions()
                                .limit(100)
                )
                .toList();
    }

    public void updateStatus(final String id, final String status) {
        final Query<DocumentDatabaseEntity> query = datastore.createQuery(DocumentDatabaseEntity.class)
                .filter("id", id);

        final UpdateOperations<DocumentDatabaseEntity> updateOperations = datastore
                .createUpdateOperations(DocumentDatabaseEntity.class)
                .set("status", status);

        datastore.update(query, updateOperations);
    }

    public void updateCompression(final String id, final String compression) {
        final Query<DocumentDatabaseEntity> query = datastore.createQuery(DocumentDatabaseEntity.class)
                .filter("id", id);

        final UpdateOperations<DocumentDatabaseEntity> updateOperations = datastore
                .createUpdateOperations(DocumentDatabaseEntity.class)
                .set("compression", compression);

        datastore.update(query, updateOperations);
    }

    public void updateFileSizeAndChecksum(final String id, final long fileSize, final String checksum) {
        final Query<DocumentDatabaseEntity> query = datastore.createQuery(DocumentDatabaseEntity.class)
                .filter("id", id);

        final UpdateOperations<DocumentDatabaseEntity> updateOperations = datastore
                .createUpdateOperations(DocumentDatabaseEntity.class)
                .set("fileSize", fileSize)
                .set("checksum", checksum);

        datastore.update(query, updateOperations);
    }

    public List<DocumentDatabaseEntity> findByChecksumAndFileSize(final String checksum, final long fileSize) {
        return datastore.createQuery(DocumentDatabaseEntity.class)
                .field("checksum").equal(checksum)
                .field("fileSize").equal(fileSize)
                .find(
                        new FindOptions()
                                .limit(100)
                )
                .toList();
    }

    public MorphiaCursor<DocumentDatabaseEntity> findAll() {
        return datastore.createQuery(DocumentDatabaseEntity.class).find();
    }
}
