package com.github.loa.downloader.service.file;

import com.github.loa.url.service.downloader.FileDownloadManager;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This service is responsible to acquire a file from an {@link URL}.
 */
@Service
@RequiredArgsConstructor
public class FileCollector {

    private final FileDownloadManager fileDownloadManager;

    public Mono<Path> acquireFile(final URL fileLocation, final Path resultLocation) {
        final String protocol = fileLocation.getProtocol();

        if ("http".equals(protocol) || "https".equals(protocol)) {
            return fileDownloadManager.downloadFile(fileLocation, resultLocation);
        } else if ("file".equals(protocol)) {
            copyFile(fileLocation, resultLocation);

            return Mono.just(resultLocation);
        }

        return Mono.empty();
    }

    @SneakyThrows
    private void copyFile(final URL fileLocation, final Path resultLocation) {
        Files.copy(Path.of(fileLocation.toURI()), resultLocation);
    }
}
