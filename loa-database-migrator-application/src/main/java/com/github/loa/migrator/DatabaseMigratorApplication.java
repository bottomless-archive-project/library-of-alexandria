package com.github.loa.migrator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DatabaseMigratorApplication {

    public static void main(final String[] args) {
        SpringApplication.run(DatabaseMigratorApplication.class, args);
    }
}
