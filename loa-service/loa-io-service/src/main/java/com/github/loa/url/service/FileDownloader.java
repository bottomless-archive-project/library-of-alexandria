package com.github.loa.url.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.net.URL;

/**
 * This service is responsible for downloading files from the internet.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileDownloader {

    private final WebClient downloaderWebClient;

    /**
     * Download a file from the provided url to the provided file location.
     *
     * @param downloadTarget the url to download the file from
     * @param resultLocation the location to download the file to
     */
    public Mono<File> downloadFile(final URL downloadTarget, final File resultLocation) {
        try {
            final Flux<DataBuffer> inputStream = downloaderWebClient.get()
                    .uri(downloadTarget.toURI())
                    .retrieve()
                    .bodyToFlux(DataBuffer.class);

            return DataBufferUtils.write(inputStream, resultLocation.toPath())
                    .doOnError(error -> resultLocation.delete())
                    .thenReturn(resultLocation);
        } catch (Exception e) {
            log.debug("Failed to download document from location: {}.", downloadTarget, e);

            return Mono.empty();
        }
    }
}
