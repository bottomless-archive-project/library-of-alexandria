package com.github.loa.indexer.command;

import com.morethanheroic.taskforce.executor.JobExecutor;
import com.morethanheroic.taskforce.job.Job;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class IndexerCommand implements CommandLineRunner {

    private final JobExecutor jobExecutor;
    private final Job indexerJob;

    @Override
    public void run(final String... args) {
        log.info("Initializing document indexing.");

        jobExecutor.execute(indexerJob);

        log.info("Finished document indexing.");
    }
}
