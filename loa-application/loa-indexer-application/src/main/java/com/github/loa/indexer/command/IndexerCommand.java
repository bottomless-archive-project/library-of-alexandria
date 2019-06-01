package com.github.loa.indexer.command;

import com.github.loa.document.service.DocumentManipulator;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.domain.DocumentStatus;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.indexer.configuration.IndexerConfigurationProperties;
import com.github.loa.indexer.service.index.IndexerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class IndexerCommand implements CommandLineRunner {

    private final DocumentEntityFactory documentEntityFactory;
    private final IndexerConfigurationProperties indexerConfigurationProperties;
    private final IndexerService indexerService;
    private final DocumentManipulator documentManipulator;

    @Override
    public void run(final String... args) {
        log.info("Initializing document indexing.");

        try {
            while (true) {
                final List<DocumentEntity> documentEntities = documentEntityFactory
                        .getDocumentEntity(DocumentStatus.DOWNLOADED);

                if (documentEntities.isEmpty()) {
                    log.info("Waiting for a while because no documents are available for indexing.");

                    Thread.sleep(indexerConfigurationProperties.getSleepTime());
                } else {
                    for (DocumentEntity documentEntity : documentEntities) {
                        if (documentEntity.getFileSize() < indexerConfigurationProperties.getMaximumFileSize()) {
                            indexerService.indexDocuments(documentEntity);
                        } else {
                            log.info("Skipping document " + documentEntity.getId() + " because it's size is too high.");

                            documentManipulator.markIndexFailure(documentEntity.getId());
                        }
                    }

                    log.info("Indexed " + documentEntities.size() + " documents!");
                }
            }
        } catch (InterruptedException e) {
            log.error("Failed to index documents!", e);
        }
    }
}
