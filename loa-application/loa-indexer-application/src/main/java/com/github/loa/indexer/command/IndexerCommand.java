package com.github.loa.indexer.command;

import com.github.loa.document.service.DocumentManipulator;
import com.github.loa.document.service.domain.DocumentStatus;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.indexer.configuration.IndexerConfigurationProperties;
import com.github.loa.indexer.service.index.IndexerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class IndexerCommand implements CommandLineRunner {

    private static final String SCHEDULER_NAME = "indexer-scheduler";

    private final DocumentEntityFactory documentEntityFactory;
    private final IndexerConfigurationProperties indexerConfigurationProperties;
    private final IndexerService indexerService;
    private final DocumentManipulator documentManipulator;

    private final Set<String> processedDocuments = new HashSet<>();

    @Override
    public void run(final String... args) {
        log.info("Initializing document indexing.");

        documentEntityFactory.getDocumentEntity(DocumentStatus.DOWNLOADED)
                .parallel(indexerConfigurationProperties.getConcurrentIndexerThreads())
                .runOn(newScheduler(indexerConfigurationProperties))
                .filter(documentEntity -> !processedDocuments.contains(documentEntity.getId()))
                .doOnNext(documentEntity -> processedDocuments.add(documentEntity.getId()))
                .subscribe(documentEntity -> {
                    if (documentEntity.getFileSize() < indexerConfigurationProperties.getMaximumFileSize()) {
                        indexerService.indexDocuments(documentEntity);
                    } else {
                        log.info("Skipping document " + documentEntity.getId() + " because it's size is too high.");

                        documentManipulator.markIndexFailure(documentEntity.getId());
                    }
                });
    }

    private Scheduler newScheduler(final IndexerConfigurationProperties indexerConfigurationProperties) {
        return Schedulers.newParallel(SCHEDULER_NAME, indexerConfigurationProperties.getConcurrentIndexerThreads());
    }
}
