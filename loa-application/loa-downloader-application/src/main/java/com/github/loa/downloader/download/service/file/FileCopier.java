package com.github.loa.downloader.download.service.file;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Service
public class FileCopier {

    public Mono<File> copyFile(final File fileLocation, final File resultLocation) {
        return Mono.fromSupplier(() -> {
            try {
                return Files.copy(fileLocation.toPath(), resultLocation.toPath()).toFile();
            } catch (IOException e) {
                throw new RuntimeException("Unable to copy file: " + fileLocation);
            }
        });
    }
}
