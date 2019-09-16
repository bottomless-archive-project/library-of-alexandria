package com.github.loa.document.repository;

import com.github.loa.document.repository.domain.DocumentDatabaseEntity;
import com.github.loa.document.repository.domain.DocumentLocationDatabaseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class DocumentLocationRepository {

    private final ReactiveMongoTemplate mongoTemplate;

    public Mono<Boolean> existsById(final String id) {
        final Query query = Query
                .query(
                        Criteria.where("_id").is(id)
                );

        return mongoTemplate.exists(query, DocumentDatabaseEntity.class);
    }

    public Mono<DocumentLocationDatabaseEntity> findById(final String id) {
        return mongoTemplate.findById(id, DocumentLocationDatabaseEntity.class);
    }

    public void insertDocumentLocation(final DocumentLocationDatabaseEntity documentLocationDatabaseEntity) {
        mongoTemplate.insert(documentLocationDatabaseEntity);
    }
}
