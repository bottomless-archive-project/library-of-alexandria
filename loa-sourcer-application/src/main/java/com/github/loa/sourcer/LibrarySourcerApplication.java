package com.github.loa.sourcer;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
public class LibrarySourcerApplication {

    public static void main(final String[] args) {
        SpringApplication.run(LibrarySourcerApplication.class, args);
    }
}
