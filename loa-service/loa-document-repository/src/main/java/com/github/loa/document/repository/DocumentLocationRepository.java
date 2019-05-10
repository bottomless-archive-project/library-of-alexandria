package com.github.loa.document.repository;

import com.github.loa.document.repository.domain.DocumentLocationDatabaseEntity;
import dev.morphia.Datastore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DocumentLocationRepository {

    private final Datastore datastore;

    public DocumentLocationDatabaseEntity findById(final String id) {
        return datastore.createQuery(DocumentLocationDatabaseEntity.class)
                .field("_id").equal(id)
                .first();
    }

    public void insertDocumentLocation(final DocumentLocationDatabaseEntity documentLocationDatabaseEntity) {
        datastore.save(documentLocationDatabaseEntity);
    }
}
