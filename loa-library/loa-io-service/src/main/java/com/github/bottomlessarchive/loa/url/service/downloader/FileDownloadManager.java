package com.github.bottomlessarchive.loa.url.service.downloader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This service is responsible for downloading files from the internet.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnBean(WebClient.class)
public class FileDownloadManager {

    private final FileDownloadRequestFactory fileDownloadRequestFactory;

    /**
     * Download a file from the provided url to the provided file location.
     *
     * @param downloadTarget the url to download the file from
     * @param resultLocation the location to download the file to
     */
    public Mono<Path> downloadFile(final URL downloadTarget, final Path resultLocation) {
        try {
            final Flux<DataBuffer> dataBufferFlux =
                    fileDownloadRequestFactory.newDownloadRequest(downloadTarget.toURI());

            return DataBufferUtils.write(dataBufferFlux, resultLocation)
                    .doOnError(error -> {
                        try {
                            Files.delete(resultLocation);
                        } catch (final IOException e) {
                            log.error("Failed to delete file at the staging location!", e);
                        }
                    })
                    .thenReturn(resultLocation);
        } catch (final URISyntaxException e) {
            log.debug("Failed to download document from location: {}.", downloadTarget, e);

            return Mono.empty();
        }
    }
}
