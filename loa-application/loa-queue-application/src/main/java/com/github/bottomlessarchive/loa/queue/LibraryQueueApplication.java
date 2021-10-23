package com.github.bottomlessarchive.loa.queue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * An application that is an intermediate between the Generator and the Downloader Application.
 */
@SpringBootApplication(scanBasePackages = "com.github.loa")
@ConfigurationPropertiesScan(basePackages = "com.github.loa")
public class LibraryQueueApplication {

    public static void main(final String[] args) {
        SpringApplication.run(LibraryQueueApplication.class, args);
    }
}
