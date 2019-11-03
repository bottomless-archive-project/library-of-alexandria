package com.github.loa.generator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The runner class of the generator application.
 */
@Slf4j
@SpringBootApplication(scanBasePackages = "com.github.loa")
public class LibraryGeneratorApplication {

    public static void main(final String[] args) {
        SpringApplication.run(LibraryGeneratorApplication.class, args);
    }
}
