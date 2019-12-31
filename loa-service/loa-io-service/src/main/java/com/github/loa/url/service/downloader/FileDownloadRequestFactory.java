package com.github.loa.url.service.downloader;

import com.github.loa.url.service.downloader.domain.RetryableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.retry.Backoff;
import reactor.retry.Jitter;
import reactor.retry.Retry;

import java.net.URI;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileDownloadRequestFactory {

    private final WebClient downloaderWebClient;

    /**
     * Create and partially execute a new high level download request that handles retries when sufficient to do a
     * retry and convert the response to a downloadable format.
     *
     * @param downloadTarget the target resource to download
     * @return the body of the response
     */
    public Flux<DataBuffer> newDownloadRequest(final URI downloadTarget) {
        return downloaderWebClient.get()
                .uri(downloadTarget)
                .exchange()
                .flatMap(clientResponse -> validateResponse(downloadTarget, clientResponse))
                .retryWhen(buildRetry())
                .flatMapMany(this::convertResponse);
    }

    private Mono<ClientResponse> validateResponse(final URI downloadTarget, final ClientResponse clientResponse) {
        if (clientResponse.statusCode() == HttpStatus.TOO_MANY_REQUESTS) {
            log.info("Too many requests for location: {}. Retrying!", downloadTarget);

            return Mono.error(new RetryableException());
        }

        return Mono.just(clientResponse);
    }

    private Retry<Object> buildRetry() {
        return Retry.anyOf(RetryableException.class)
                .jitter(Jitter.random())
                .backoff(Backoff.exponential(Duration.ofSeconds(5), Duration.ofMinutes(2), 2, true))
                .retryMax(10);
    }

    private Flux<DataBuffer> convertResponse(final ClientResponse clientResponse) {
        return clientResponse.bodyToFlux(DataBuffer.class);
    }
}
