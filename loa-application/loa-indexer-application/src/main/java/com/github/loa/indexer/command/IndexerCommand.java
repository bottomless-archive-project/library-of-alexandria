package com.github.loa.indexer.command;

import com.github.loa.indexer.batch.generator.DownloadedDocumentEntityGenerator;
import com.github.loa.indexer.batch.task.IndexerTask;
import com.morethanheroic.taskforce.executor.JobExecutor;
import com.morethanheroic.taskforce.job.Job;
import com.morethanheroic.taskforce.job.builder.JobBuilder;
import com.morethanheroic.taskforce.sink.DiscardingSink;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class IndexerCommand implements CommandLineRunner {

    private final JobExecutor jobExecutor;
    private final DownloadedDocumentEntityGenerator downloadedDocumentEntityGenerator;
    private final IndexerTask indexerTask;

    @Override
    public void run(final String... args) {
        log.info("Initializing document indexing.");

        final Job job = JobBuilder.newBuilder()
                .generator(downloadedDocumentEntityGenerator)
                .task(indexerTask)
                .sink(DiscardingSink.of())
                .withThreadCount(1)
                .build();

        jobExecutor.execute(job);

        log.info("Finished document indexing.");

        /*
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
        */
    }
}
