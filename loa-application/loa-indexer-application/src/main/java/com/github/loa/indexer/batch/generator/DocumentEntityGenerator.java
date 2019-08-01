package com.github.loa.indexer.batch.generator;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.domain.DocumentStatus;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.indexer.configuration.IndexerConfigurationProperties;
import com.morethanheroic.taskforce.generator.Generator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class DocumentEntityGenerator implements Generator<DocumentEntity> {

    private final DocumentStatus documentStatus;
    private final DocumentEntityFactory documentEntityFactory;
    private final IndexerConfigurationProperties indexerConfigurationProperties;

    private List<DocumentEntity> documentEntities;
    private Set<String> processedDocuments = new HashSet<>();

    @Override
    public Optional<DocumentEntity> generate() {
        while (shouldQueryMoreEntities()) {
            documentEntities = documentEntityFactory.getDocumentEntity(documentStatus).stream()
                    .filter(documentEntity -> !processedDocuments.contains(documentEntity.getId()))
                    .peek(documentEntity -> processedDocuments.add(documentEntity.getId()))
                    .collect(Collectors.toList());

            log.info("Added " + documentEntities.size() + " new documents for indexing.");

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

    private boolean shouldQueryMoreEntities() {
        return documentEntities == null || documentEntities.isEmpty();
    }
}
