package com.github.loa.location.repository;

import com.github.loa.location.repository.domain.DocumentLocationDatabaseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class DocumentLocationRepository {

    private final ReactiveMongoTemplate mongoTemplate;

    public Mono<Boolean> existsOrInsert(
            final DocumentLocationDatabaseEntity documentLocationDatabaseEntity) {
        final Query query = Query
                .query(
                        Criteria.where("_id").is(documentLocationDatabaseEntity.getId())
                );

        final Update update = new Update()
                .setOnInsert("_id", documentLocationDatabaseEntity.getId())
                .setOnInsert("url", documentLocationDatabaseEntity.getUrl())
                .setOnInsert("source", documentLocationDatabaseEntity.getSource())
                .setOnInsert("downloaderVersion", documentLocationDatabaseEntity.getDownloaderVersion());

        return mongoTemplate.upsert(query, update, DocumentLocationDatabaseEntity.class)
                .map(updateResult -> updateResult.getMatchedCount() != 0);
    }
}
