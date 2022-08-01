package com.github.bottomlessarchive.loa.location.repository;

import com.github.bottomlessarchive.loa.location.repository.domain.DocumentLocationDatabaseEntity;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

@Component
@RequiredArgsConstructor
public class DocumentLocationRepository {

    private static final int DUPLICATE_DOCUMENT_ID_ERROR_CODE = 11000;

    private final MongoCollection<DocumentLocationDatabaseEntity> documentLocationDatabaseEntityMongoCollection;

    public Optional<DocumentLocationDatabaseEntity> getById(final byte[] id) {
        return Optional.ofNullable(documentLocationDatabaseEntityMongoCollection.find(eq("_id", id)).first());
    }

    public boolean existsOrInsert(final DocumentLocationDatabaseEntity documentLocationDatabaseEntity) {
        try {
            documentLocationDatabaseEntityMongoCollection.insertOne(documentLocationDatabaseEntity);

            return false;
        } catch (final MongoWriteException e) {
            if (e.getError().getCode() == DUPLICATE_DOCUMENT_ID_ERROR_CODE) {
                return true;
            }

            throw e;
        }
    }

    public void updateDownloadResultCode(final String documentLocationId, final int downloadResultCode) {
        documentLocationDatabaseEntityMongoCollection.updateOne(eq("_id", documentLocationId),
                set("downloadResultCode", downloadResultCode));
    }
}
