package com.github.loa.location.repository;

import com.github.loa.location.repository.domain.DocumentLocationDatabaseEntity;
import com.mongodb.MongoWriteException;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class DocumentLocationRepository {

    private final MongoDatabase mongoDatabase;

    public Mono<Boolean> existsOrInsert(final DocumentLocationDatabaseEntity documentLocationDatabaseEntity) {
        final MongoCollection<DocumentLocationDatabaseEntity> documentLocationCollection =
                mongoDatabase.getCollection("documentLocation", DocumentLocationDatabaseEntity.class);

        return Mono.from(documentLocationCollection.insertOne(documentLocationDatabaseEntity))
                .map(result -> Boolean.FALSE)
                .onErrorReturn((throwable -> throwable instanceof MongoWriteException
                        && throwable.getMessage().startsWith("E11000 duplicate key error")), Boolean.TRUE);
    }
}
