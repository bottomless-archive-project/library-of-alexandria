package com.github.loa.queue;

import com.github.loa.queue.configuration.QueueConfigurationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * An application that is an intermediate between the Generator and the Downloader Application.
 */
@SpringBootApplication(scanBasePackages = "com.github.loa")
@EnableConfigurationProperties(QueueConfigurationProperties.class)
public class LibraryQueueApplication {

    public static void main(final String[] args) {
        SpringApplication.run(LibraryQueueApplication.class, args);
    }
}
