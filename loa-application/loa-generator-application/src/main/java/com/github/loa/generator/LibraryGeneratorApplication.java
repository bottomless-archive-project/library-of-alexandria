package com.github.loa.generator;

import com.github.loa.queue.configuration.QueueConfigurationProperties;
import com.github.loa.repository.configuration.RepositoryConfigurationProperties;
import com.github.loa.source.commoncrawl.configuration.CommonCrawlDocumentSourceConfigurationProperties;
import com.github.loa.source.configuration.DocumentSourceConfiguration;
import com.github.loa.source.file.configuration.FileDocumentSourceConfigurationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * The runner class of the generator application.
 */
@EnableScheduling
@EnableConfigurationProperties({QueueConfigurationProperties.class, RepositoryConfigurationProperties.class,
        DocumentSourceConfiguration.class, CommonCrawlDocumentSourceConfigurationProperties.class,
        FileDocumentSourceConfigurationProperties.class
})
@SpringBootApplication(scanBasePackages = "com.github.loa")
public class LibraryGeneratorApplication {

    public static void main(final String[] args) {
        SpringApplication.run(LibraryGeneratorApplication.class, args);
    }
}
