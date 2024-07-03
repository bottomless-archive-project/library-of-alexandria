package com.github.bottomlessarchive.loa.batch.service.entity.transformer;

import com.github.bottomlessarchive.loa.batch.repository.domain.BatchDatabaseEntity;
import com.github.bottomlessarchive.loa.batch.service.domain.BatchEntity;
import com.github.bottomlessarchive.loa.batch.service.domain.BatchStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BatchEntityTransformer {

    public BatchEntity transform(final BatchDatabaseEntity batchDatabaseEntity) {
        return BatchEntity.builder()
                .id(batchDatabaseEntity.id())
                .status(BatchStatus.valueOf(batchDatabaseEntity.status()))
                .documents(batchDatabaseEntity.documents())
                .build();
    }
}
