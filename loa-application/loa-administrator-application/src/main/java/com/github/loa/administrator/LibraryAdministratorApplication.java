package com.github.loa.administrator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * This application contains useful commands that manipulates either the database or the vault. These commands are
 * usually batch processing in nature and fixes/corrects errors, remove certain documents etc.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@SpringBootApplication(scanBasePackages = "com.github.loa")
public class LibraryAdministratorApplication {

    public static void main(final String[] args) {
        SpringApplication.run(LibraryAdministratorApplication.class, args);
    }
}
