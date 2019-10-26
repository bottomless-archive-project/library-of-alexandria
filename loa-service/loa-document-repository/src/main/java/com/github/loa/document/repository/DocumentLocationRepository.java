package com.github.loa.document.repository;

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

        return mongoTemplate.exists(query, DocumentLocationDatabaseEntity.class);
    }

    public Mono<DocumentLocationDatabaseEntity> insertDocumentLocation(
            final DocumentLocationDatabaseEntity documentLocationDatabaseEntity) {
        return mongoTemplate.insert(documentLocationDatabaseEntity);
    }
}
