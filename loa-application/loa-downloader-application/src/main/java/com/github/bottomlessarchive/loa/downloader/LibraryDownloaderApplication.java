package com.github.bottomlessarchive.loa.downloader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * The runner class of the downloader application.
 */
@EnableRetry
@EnableScheduling
@SpringBootApplication(scanBasePackages = "com.github.bottomlessarchive.loa")
@ConfigurationPropertiesScan(basePackages = "com.github.bottomlessarchive.loa")
public class LibraryDownloaderApplication {

    public static void main(final String[] args) {
        SpringApplication.run(LibraryDownloaderApplication.class, args);
    }
}
