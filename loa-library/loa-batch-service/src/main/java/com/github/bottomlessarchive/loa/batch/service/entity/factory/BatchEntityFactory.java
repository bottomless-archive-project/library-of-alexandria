package com.github.bottomlessarchive.loa.batch.service.entity.factory;

import com.github.bottomlessarchive.loa.batch.repository.BatchRepository;
import com.github.bottomlessarchive.loa.batch.repository.domain.BatchDatabaseEntity;
import com.github.bottomlessarchive.loa.batch.service.domain.BatchEntity;
import com.github.bottomlessarchive.loa.batch.service.domain.BatchStatus;
import com.github.bottomlessarchive.loa.batch.service.entity.transformer.BatchEntityTransformer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BatchEntityFactory {

    private final BatchRepository batchRepository;
    private final BatchEntityTransformer batchEntityTransformer;

    public Optional<BatchEntity> getNextBatch() {
        return batchRepository.findByStatus(BatchStatus.CREATED.name())
                .map(batchEntityTransformer::transform);
    }

    public BatchEntity newBatchEntity() {
        final BatchDatabaseEntity batchDatabaseEntity = BatchDatabaseEntity.builder()
                .id(UUID.randomUUID())
                .status(BatchStatus.CREATED.name())
                .documents(Collections.emptyList())
                .build();

        batchRepository.insertBatch(batchDatabaseEntity);

        return batchEntityTransformer.transform(batchDatabaseEntity);
    }
}
