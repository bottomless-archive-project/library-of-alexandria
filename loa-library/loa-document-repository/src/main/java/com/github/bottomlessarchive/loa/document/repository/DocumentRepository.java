package com.github.bottomlessarchive.loa.document.repository;

import com.github.bottomlessarchive.loa.document.repository.domain.DocumentDatabaseEntity;
import com.mongodb.client.model.Filters;
import com.mongodb.reactivestreams.client.MongoCollection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

@Component
@RequiredArgsConstructor
public class DocumentRepository {

    private final MongoCollection<DocumentDatabaseEntity> documentDatabaseEntityMongoCollection;

    /**
     * Update the status of the documents available in the repository to the provided value.
     *
     * @param status the value to update the status of the documents to
     * @return the result of the update
     */
    public Mono<Void> updateStatus(final String status) {
        return Mono.from(documentDatabaseEntityMongoCollection
                        .updateMany(Filters.empty(), set("status", status)))
                .then();
    }

    public Mono<Void> updateStatus(final UUID documentId, final String status) {
        return Mono.from(documentDatabaseEntityMongoCollection
                        .updateOne(eq("_id", documentId), set("status", status)))
                .then();
    }
}
