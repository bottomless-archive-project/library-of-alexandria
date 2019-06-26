package com.github.loa.downloader.command;

import com.github.loa.downloader.command.batch.filter.DocumentLocationRecordFilter;
import com.github.loa.downloader.command.batch.mapper.DocumentLocationEncoderRecordMapper;
import com.github.loa.downloader.command.batch.mapper.DocumentLocationRecordMapper;
import com.github.loa.downloader.command.batch.processor.DocumentLocationRecordProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.easybatch.core.job.Job;
import org.easybatch.core.job.JobBuilder;
import org.easybatch.core.job.JobExecutor;
import org.easybatch.core.reader.RecordReader;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DownloaderCommand implements CommandLineRunner {

    private final RecordReader recordReader;
    private final DocumentLocationRecordFilter documentLocationRecordFilter;
    private final DocumentLocationRecordMapper documentLocationRecordMapper;
    private final DocumentLocationEncoderRecordMapper documentLocationEncoderRecordMapper;
    private final DocumentLocationRecordProcessor documentLocationRecordProcessor;

    @Override
    public void run(final String... args) {
        log.info("Initializing document processing.");

        final Job job = JobBuilder.aNewJob()
                .reader(recordReader)
                .mapper(documentLocationRecordMapper)
                .filter(documentLocationRecordFilter)
                .mapper(documentLocationEncoderRecordMapper)
                .processor(documentLocationRecordProcessor)
                .build();

        final JobExecutor jobExecutor = new JobExecutor(30);
        jobExecutor.execute(job);
        jobExecutor.shutdown();
        //documentSourceProcessor.processDocumentSource(documentSourceProviderFactory.openSource());
    }
}
