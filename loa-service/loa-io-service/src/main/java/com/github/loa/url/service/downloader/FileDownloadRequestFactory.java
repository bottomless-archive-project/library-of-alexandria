package com.github.loa.url.service.downloader;

import com.github.loa.url.service.downloader.domain.RetryableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnBean(FileDownloadManager.class)
class FileDownloadRequestFactory {

    private final WebClient downloaderWebClient;
    private final FileDownloadRetryFactory fileDownloadRetryFactory;

    /**
     * Create and partially execute a new high level download request that handles retries when sufficient to do a
     * retry and convert the response to a downloadable format.
     *
     * @param downloadTarget the target resource to download
     * @return the body of the response
     */
    Flux<DataBuffer> newDownloadRequest(final URI downloadTarget) {
        return downloaderWebClient.get()
                .uri(downloadTarget)
                .exchange()
                .flatMap(clientResponse -> validateResponse(downloadTarget, clientResponse))
                .retryWhen(fileDownloadRetryFactory.newRetry())
                .flatMapMany(this::convertResponse);
    }

    private Mono<ClientResponse> validateResponse(final URI downloadTarget, final ClientResponse clientResponse) {
        if (clientResponse.statusCode() == HttpStatus.TOO_MANY_REQUESTS) {
            log.debug("Too many requests for location: {}. Retrying!", downloadTarget);

            return Mono.error(new RetryableException());
        }

        return Mono.just(clientResponse);
    }

    private Flux<DataBuffer> convertResponse(final ClientResponse clientResponse) {
        return clientResponse.bodyToFlux(DataBuffer.class);
    }
}
