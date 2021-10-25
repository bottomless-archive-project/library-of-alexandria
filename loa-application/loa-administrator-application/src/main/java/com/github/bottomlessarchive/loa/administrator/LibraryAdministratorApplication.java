package com.github.bottomlessarchive.loa.administrator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * This application contains useful commands that manipulates either the database or the vault. These commands are
 * usually batch processing in nature and fixes/corrects errors, remove certain documents etc.
 */
@SpringBootApplication(scanBasePackages = "com.github.bottomlessarchive.loa")
@ConfigurationPropertiesScan(basePackages = "com.github.bottomlessarchive.loa")
public class LibraryAdministratorApplication {

    public static void main(final String[] args) {
        SpringApplication.run(LibraryAdministratorApplication.class, args);
    }
}
