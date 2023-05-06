package com.github.bottomlessarchive.loa.downloader.service.source.beacon.loadocloader.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class LoadocLoaderBeaconConfiguration {

    /*@Bean
    public ExecutorService downloaderExecutorService() {
        //TODO: This should be configurable
        return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    }*/
}
