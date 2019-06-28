package com.github.loa.downloader.command.configuration;

import com.morethanheroic.taskforce.executor.JobExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JobExecutorConfiguration {

    @Bean
    public JobExecutor jobExecutor() {
        return new JobExecutor();
    }
}
