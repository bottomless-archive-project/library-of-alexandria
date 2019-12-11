package com.github.loa.downloader;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.reactive.function.client.ClientHttpConnectorAutoConfiguration;

/**
 * The runner class of the downloader application.
 */
@Slf4j
@SpringBootApplication(scanBasePackages = "com.github.loa", exclude = ClientHttpConnectorAutoConfiguration.class)
public class LibraryDownloaderApplication {

    public static void main(final String[] args) {
        SpringApplication.run(LibraryDownloaderApplication.class, args);
    }
}
