package com.github.bottomlessarchive.loa.indexer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(scanBasePackages = "com.github.bottomlessarchive.loa")
@ConfigurationPropertiesScan(basePackages = "com.github.bottomlessarchive.loa")
public class LibraryIndexerApplication {

    public static void main(final String[] args) {
        SpringApplication.run(LibraryIndexerApplication.class, args);
    }
}
