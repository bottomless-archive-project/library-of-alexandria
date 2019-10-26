package com.github.loa.downloader.command;

import com.github.loa.downloader.command.batch.DocumentLocationFactory;
import com.github.loa.downloader.command.batch.task.DocumentLocationConverterTask;
import com.github.loa.downloader.command.batch.task.DocumentLocationEncoderTask;
import com.github.loa.downloader.command.batch.task.DocumentLocationFilterTask;
import com.github.loa.downloader.command.batch.task.DocumentLocationProcessorTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DownloaderCommand implements CommandLineRunner {

    private final DocumentLocationConverterTask documentLocationConverterTask;
    private final DocumentLocationFilterTask documentLocationFilterTask;
    private final DocumentLocationEncoderTask documentLocationEncoderTask;
    private final DocumentLocationProcessorTask documentLocationRecordProcessor;

    private final DocumentLocationFactory documentLocationFactory;

    @Override
    public void run(final String... args) {
        log.info("Initializing document processing.");

        documentLocationFactory.streamLocations()
                .flatMap(documentLocationConverterTask::execute)
                .filter(documentLocationFilterTask::execute)
                .flatMap(documentLocationEncoderTask::execute)
                .subscribe(documentLocationRecordProcessor::execute);

        /*final Job job = JobBuilder.newBuilder()
                .generator(generator)
                .task(documentLocationConverterTask)
                .task(documentLocationFilterTask)
                .task(documentLocationEncoderTask)
                .task(documentLocationRecordProcessor)
                .sink(DiscardingSink.of())
                .withThreadCount(downloaderExecutorConfigurationProperties.getThreadCount())
                .build();

        jobExecutor.execute(job);*/
    }
}
