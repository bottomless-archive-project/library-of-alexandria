package com.github.bottomlessarchive.loa.batch.repository;

import com.github.bottomlessarchive.loa.batch.repository.domain.BatchDatabaseEntity;
import com.mongodb.client.MongoCollection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.mongodb.client.model.Filters.eq;

@Component
@RequiredArgsConstructor
public class BatchRepository {

    private final MongoCollection<BatchDatabaseEntity> batchDatabaseEntityMongoCollection;

    public void insertBatch(final BatchDatabaseEntity batchDatabaseEntity) {
        batchDatabaseEntityMongoCollection.insertOne(batchDatabaseEntity);
    }

    public Optional<BatchDatabaseEntity> findByStatus(final String status) {
        return Optional.ofNullable(batchDatabaseEntityMongoCollection.find(eq("status", status)).first());
    }
}
