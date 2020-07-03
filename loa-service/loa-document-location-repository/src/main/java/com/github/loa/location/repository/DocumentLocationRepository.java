package com.github.loa.location.repository;

import com.github.loa.location.repository.domain.DocumentLocationDatabaseEntity;
import com.mongodb.MongoWriteException;
import com.mongodb.reactivestreams.client.MongoCollection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class DocumentLocationRepository {

    private final MongoCollection<DocumentLocationDatabaseEntity> documentLocationDatabaseEntityMongoCollection;

    public Mono<Boolean> existsOrInsert(final DocumentLocationDatabaseEntity documentLocationDatabaseEntity) {
        return Mono.from(documentLocationDatabaseEntityMongoCollection.insertOne(documentLocationDatabaseEntity))
                .map(result -> Boolean.FALSE)
                .onErrorReturn(throwable -> throwable instanceof MongoWriteException
                        && throwable.getMessage().startsWith("E11000 duplicate key error"), Boolean.TRUE);
    }

    public Mono<Long> count() {
        return Mono.from(documentLocationDatabaseEntityMongoCollection.countDocuments());
    }
}
