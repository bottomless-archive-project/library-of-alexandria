package com.github.loa.downloader.download.service.file;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

/**
 * 0this device is responsible to acquire a file from the an {@link URL}.
 */
@Service
@RequiredArgsConstructor
public class FileCollector {

    private final FileCopier fileCopier;
    private final FileDownloader fileDownloader;

    public Mono<File> acquireFile(final URL fileLocation, final File resultLocation) {
        final String protocol = fileLocation.getProtocol();

        if ("file".equals(protocol)) {
            try {
                return fileCopier.copyFile(Paths.get(fileLocation.toURI()).toFile(), resultLocation);
            } catch (URISyntaxException e) {
                throw new RuntimeException("Unable to copy file: " + fileLocation);
            }
        } else if ("http".equals(protocol) || "https".equals(protocol)) {
            return fileDownloader.downloadFile(fileLocation, resultLocation);
        }

        return Mono.empty();
    }
}
