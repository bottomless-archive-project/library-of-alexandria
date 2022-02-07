package com.github.bottomlessarchive.loa.location.repository;

import com.github.bottomlessarchive.loa.location.repository.domain.DocumentLocationDatabaseEntity;
import com.mongodb.client.MongoCollection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.mongodb.client.model.Filters.eq;

@Component
@RequiredArgsConstructor
public class DocumentLocationSyncRepository {

    private final MongoCollection<DocumentLocationDatabaseEntity> documentLocationDatabaseEntityMongoCollection;

    public Optional<DocumentLocationDatabaseEntity> getById(final byte[] id) {
        return Optional.ofNullable(documentLocationDatabaseEntityMongoCollection.find(eq("_id", id)).first());
    }
}
