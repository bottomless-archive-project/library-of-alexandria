package com.github.loa.downloader.command;

import com.github.loa.downloader.command.batch.task.DocumentLocationConverterTask;
import com.github.loa.downloader.command.batch.task.DocumentLocationEncoderTask;
import com.github.loa.downloader.command.batch.task.DocumentLocationFilterTask;
import com.github.loa.downloader.command.batch.task.DocumentLocationProcessorTask;
import com.github.loa.downloader.command.configuration.DownloaderExecutorConfigurationProperties;
import com.morethanheroic.taskforce.executor.JobExecutor;
import com.morethanheroic.taskforce.generator.Generator;
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
public class DownloaderCommand implements CommandLineRunner {

    private final DownloaderExecutorConfigurationProperties downloaderExecutorConfigurationProperties;

    private final Generator<String> generator;
    private final DocumentLocationConverterTask documentLocationConverterTask;
    private final DocumentLocationFilterTask documentLocationFilterTask;
    private final DocumentLocationEncoderTask documentLocationEncoderTask;
    private final DocumentLocationProcessorTask documentLocationRecordProcessor;

    private final JobExecutor jobExecutor;

    @Override
    public void run(final String... args) {
        log.info("Initializing document processing.");

        final Job job = JobBuilder.newBuilder()
                .generator(generator)
                .task(documentLocationConverterTask)
                .task(documentLocationFilterTask)
                .task(documentLocationEncoderTask)
                .task(documentLocationRecordProcessor)
                .sink(DiscardingSink.of())
                .withThreadCount(downloaderExecutorConfigurationProperties.getThreadCount())
                .build();

        jobExecutor.execute(job);
    }
}
