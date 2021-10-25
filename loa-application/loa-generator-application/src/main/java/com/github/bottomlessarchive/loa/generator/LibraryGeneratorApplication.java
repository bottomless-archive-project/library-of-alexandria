package com.github.bottomlessarchive.loa.generator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * The runner class of the generator application.
 */
@EnableScheduling
@SpringBootApplication(scanBasePackages = "com.github.bottomlessarchive.loa")
@ConfigurationPropertiesScan(basePackages = "com.github.bottomlessarchive.loa")
public class LibraryGeneratorApplication {

    public static void main(final String[] args) {
        SpringApplication.run(LibraryGeneratorApplication.class, args);
    }
}
