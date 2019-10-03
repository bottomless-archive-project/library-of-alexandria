package com.github.loa.administrator.command.document.pagecount;

import com.github.loa.administrator.command.document.validator.DocumentValidatorConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Configuration
public class PageCountConfiguration {

    private final static String SCHEDULER_NAME = "page-count-parser-scheduler";

    @Bean
    public Scheduler pageCountScheduler(
            final DocumentValidatorConfigurationProperties documentValidatorConfigurationProperties) {
        return Schedulers.newParallel(SCHEDULER_NAME, documentValidatorConfigurationProperties.getParallelismLevel());
    }
}
