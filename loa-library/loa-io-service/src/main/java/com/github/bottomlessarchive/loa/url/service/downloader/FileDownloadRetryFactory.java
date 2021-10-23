package com.github.bottomlessarchive.loa.url.service.downloader;

import com.github.bottomlessarchive.loa.url.service.downloader.domain.RetryableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import reactor.util.retry.Retry;

import java.time.Duration;

@Slf4j
@Service
@ConditionalOnBean(FileDownloadRequestFactory.class)
class FileDownloadRetryFactory {

    /**
     * Create a {@link Retry} that retries 3 times when the exception is a {@link RetryableException}. The initial backoff is 2 seconds
     * while the maximum backoff is 2 minutes.
     *
     * @return the new retry
     */
    Retry newRetry() {
        return Retry.backoff(3, Duration.ofSeconds(5))
                .maxBackoff(Duration.ofMinutes(2))
                .filter(throwable -> {
                    if (shouldRetry(throwable)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Got exception when downloading: {}! Attempting to retry!",
                                    throwable.getClass().getName());
                        }

                        return true;
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("Got exception when downloading: {}!", throwable.getClass().getName());
                        }

                        return false;
                    }
                });
    }

    private boolean shouldRetry(final Throwable throwable) {
        return throwable instanceof RetryableException;
    }
}
