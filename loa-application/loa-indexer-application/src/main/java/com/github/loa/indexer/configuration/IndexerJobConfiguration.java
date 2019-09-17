package com.github.loa.indexer.configuration;

import com.github.loa.document.service.domain.DocumentStatus;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.indexer.batch.task.IndexerTask;
import com.morethanheroic.taskforce.executor.JobExecutor;
import com.morethanheroic.taskforce.generator.StreamGenerator;
import com.morethanheroic.taskforce.job.Job;
import com.morethanheroic.taskforce.job.builder.JobBuilder;
import com.morethanheroic.taskforce.sink.DiscardingSink;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class IndexerJobConfiguration {

    private final DocumentEntityFactory documentEntityFactory;
    private final Set<String> processedDocuments = new HashSet<>();

    @Bean
    public JobExecutor jobExecutor() {
        return new JobExecutor();
    }

    @Bean
    public Job indexerJob(final IndexerConfigurationProperties indexerConfigurationProperties,
            final IndexerTask indexerTask) {
        return JobBuilder.newBuilder()
                .generator(StreamGenerator.of(
                        documentEntityFactory.getDocumentEntity(DocumentStatus.DOWNLOADED).toStream()
                                .filter(documentEntity -> !processedDocuments.contains(documentEntity.getId()))
                                .peek(documentEntity -> processedDocuments.add(documentEntity.getId()))
                ))
                .task(indexerTask)
                .sink(DiscardingSink.of())
                .withThreadCount(indexerConfigurationProperties.getConcurrentIndexerThreads())
                .build();
    }
}
