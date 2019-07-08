package com.github.loa.indexer.batch.generator;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.domain.DocumentStatus;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.indexer.configuration.IndexerConfigurationProperties;
import com.morethanheroic.taskforce.generator.Generator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DownloadedDocumentEntityGenerator implements Generator<DocumentEntity> {

    private final DocumentEntityFactory documentEntityFactory;
    private final IndexerConfigurationProperties indexerConfigurationProperties;

    private List<DocumentEntity> documentEntities;

    @Override
    public Optional<DocumentEntity> generate() {
        if (documentEntities == null || documentEntities.size() == 0) {
            log.info("Requesting new documents for indexing.");

            documentEntities = documentEntityFactory.getDocumentEntity(DocumentStatus.DOWNLOADED);

            if (documentEntities.isEmpty()) {
                log.info("Waiting for a while because no documents are available for indexing.");

                try {
                    Thread.sleep(indexerConfigurationProperties.getSleepTime());
                } catch (InterruptedException e) {
                    log.error("Unable to sleep!");
                }
            }
        }

        return Optional.of(documentEntities.remove(0));
    }
}
