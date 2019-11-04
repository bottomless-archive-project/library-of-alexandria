package com.github.loa.queue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.github.loa")
public class LibraryQueueApplication {

    public static void main(final String[] args) {
        SpringApplication.run(LibraryQueueApplication.class, args);
    }
}
