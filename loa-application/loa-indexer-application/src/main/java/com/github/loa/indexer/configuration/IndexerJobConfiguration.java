package com.github.loa.indexer.configuration;

import com.github.loa.document.service.domain.DocumentStatus;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.indexer.batch.generator.DocumentEntityGenerator;
import com.github.loa.indexer.batch.task.IndexerTask;
import com.morethanheroic.taskforce.executor.JobExecutor;
import com.morethanheroic.taskforce.job.Job;
import com.morethanheroic.taskforce.job.builder.JobBuilder;
import com.morethanheroic.taskforce.sink.DiscardingSink;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IndexerJobConfiguration {

    @Bean
    public JobExecutor jobExecutor() {
        return new JobExecutor();
    }

    @Bean
    public Job indexerJob(final DocumentEntityGenerator documentEntityGenerator, final IndexerTask indexerTask,
            final IndexerConfigurationProperties indexerConfigurationProperties) {
        return JobBuilder.newBuilder()
                .generator(documentEntityGenerator)
                .task(indexerTask)
                .sink(DiscardingSink.of())
                .withThreadCount(indexerConfigurationProperties.getConcurrentIndexerThreads())
                .build();
    }

    @Bean
    protected DocumentEntityGenerator downloadedDocumentEntityGenerator(
            final DocumentEntityFactory documentEntityFactory,
            final IndexerConfigurationProperties indexerConfigurationProperties) {
        return new DocumentEntityGenerator(DocumentStatus.DOWNLOADED, documentEntityFactory,
                indexerConfigurationProperties);
    }
}
