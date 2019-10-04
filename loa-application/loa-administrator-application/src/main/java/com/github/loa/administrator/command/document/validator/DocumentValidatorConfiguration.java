package com.github.loa.administrator.command.document.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.loa.document.service.DocumentCollector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.File;

@Configuration
public class DocumentValidatorConfiguration {

    private final static String SCHEDULER_NAME = "document-validator-scheduler";

    @Bean
    public DocumentCollector validatorDocumentCollector(final ObjectMapper objectMapper,
            final DocumentValidatorConfigurationProperties documentValidatorConfigurationProperties) {
        return new DocumentCollector(new File(documentValidatorConfigurationProperties.getCollectorFile()),
                objectMapper);
    }

    @Bean
    public Scheduler validatorScheduler(
            final DocumentValidatorConfigurationProperties documentValidatorConfigurationProperties) {
        return Schedulers.newParallel(SCHEDULER_NAME, documentValidatorConfigurationProperties.getParallelismLevel());
    }
}
