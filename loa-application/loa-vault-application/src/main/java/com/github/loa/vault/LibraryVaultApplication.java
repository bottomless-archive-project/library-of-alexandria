package com.github.loa.vault;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.github.loa")
public class LibraryVaultApplication {

    public static void main(final String[] args) {
        SpringApplication.run(LibraryVaultApplication.class, args);
    }
}
