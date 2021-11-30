package com.github.bottomlessarchive.loa.location.repository;

import com.github.bottomlessarchive.loa.location.repository.domain.DocumentLocationDatabaseEntity;
import com.mongodb.MongoWriteException;
import com.mongodb.reactivestreams.client.MongoCollection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static com.mongodb.client.model.Filters.eq;

@Component
@RequiredArgsConstructor
public class DocumentLocationRepository {

    private static final int DUPLICATE_DOCUMENT_ID_ERROR_CODE = 11000;

    private final MongoCollection<DocumentLocationDatabaseEntity> documentLocationDatabaseEntityMongoCollection;

    public Mono<DocumentLocationDatabaseEntity> getById(byte[] id) {
        return Mono.from(documentLocationDatabaseEntityMongoCollection.find(eq("_id", id)));
    }

    public Mono<Boolean> existsOrInsert(final DocumentLocationDatabaseEntity documentLocationDatabaseEntity) {
        return Mono.from(documentLocationDatabaseEntityMongoCollection.insertOne(documentLocationDatabaseEntity))
                .map(result -> Boolean.FALSE)
                .onErrorReturn(this::isDuplicateDocumentIdError, Boolean.TRUE);
    }

    public Mono<Long> count() {
        return Mono.from(documentLocationDatabaseEntityMongoCollection.countDocuments());
    }

    private boolean isDuplicateDocumentIdError(final Throwable throwable) {
        return throwable instanceof MongoWriteException mongoWriteException
                && mongoWriteException.getError().getCode() == DUPLICATE_DOCUMENT_ID_ERROR_CODE;
    }
}
