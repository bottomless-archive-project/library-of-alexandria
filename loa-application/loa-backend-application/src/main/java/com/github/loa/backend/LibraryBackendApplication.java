package com.github.loa.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.github.loa")
public class LibraryBackendApplication {

    public static void main(final String[] args) {
        SpringApplication.run(LibraryBackendApplication.class, args);
    }
}
