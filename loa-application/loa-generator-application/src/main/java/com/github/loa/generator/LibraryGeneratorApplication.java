package com.github.loa.generator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The runner class of the generator application.
 */
@SpringBootApplication(scanBasePackages = "com.github.loa")
public class LibraryGeneratorApplication {

    public static void main(final String[] args) {
        SpringApplication.run(LibraryGeneratorApplication.class, args);
    }
}
