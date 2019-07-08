package com.github.loa.indexer.batch.task;

import com.github.loa.document.service.DocumentManipulator;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.indexer.configuration.IndexerConfigurationProperties;
import com.github.loa.indexer.service.index.IndexerService;
import com.morethanheroic.taskforce.task.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndexerTask implements Task<DocumentEntity, DocumentEntity> {

    private final IndexerConfigurationProperties indexerConfigurationProperties;
    private final DocumentManipulator documentManipulator;
    private final IndexerService indexerService;

    @Override
    public Optional<DocumentEntity> execute(final DocumentEntity documentEntity) {
        if (documentEntity.getFileSize() < indexerConfigurationProperties.getMaximumFileSize()) {
            indexerService.indexDocuments(documentEntity);

            return Optional.of(documentEntity);
        } else {
            log.info("Skipping document " + documentEntity.getId() + " because it's size is too high.");

            documentManipulator.markIndexFailure(documentEntity.getId());

            return Optional.empty();
        }
    }
}
