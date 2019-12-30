package com.github.loa.url.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.retry.Backoff;
import reactor.retry.Jitter;
import reactor.retry.Retry;

import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;

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
    public Mono<Path> downloadFile(final URL downloadTarget, final Path resultLocation) {
        try {
            final Flux<DataBuffer> dataBufferFlux = downloaderWebClient.get()
                    .uri(downloadTarget.toURI())
                    .exchange()
                    .doOnNext(clientResponse -> {
                        if (clientResponse.statusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                            log.info("Too many requests for location: {}. Retrying!", downloadTarget);

                            throw new TooManyRequestException();
                        }
                    })
                    .retryWhen(
                            Retry.anyOf(TooManyRequestException.class)
                                    .jitter(Jitter.random())
                                    .backoff(Backoff.exponential(Duration.ofSeconds(5), Duration.ofMinutes(2), 2, true))
                                    .retryMax(10)
                    )
                    .flatMapMany(clientResponse -> clientResponse.bodyToFlux(DataBuffer.class));

            return DataBufferUtils.write(dataBufferFlux, resultLocation)
                    .doOnError(error -> resultLocation.toFile().delete())
                    .thenReturn(resultLocation);
        } catch (Exception e) {
            log.debug("Failed to download document from location: {}.", downloadTarget, e);

            return Mono.empty();
        }
    }

    private static class TooManyRequestException extends RuntimeException {
    }
}
