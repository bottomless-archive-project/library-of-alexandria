package com.github.loa.downloader.service.url;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;

@Slf4j
@Service
public class URLConverter {

    public Mono<URL> convert(final String recordUrl) {
        try {
            return Mono.just(new URL(recordUrl));
        } catch (MalformedURLException e) {
            log.debug("Unable to parse url with location: " + recordUrl, e);

            return Mono.empty();
        }
    }

    public Mono<URL> convertOrThrow(final String recordUrl) {
        try {
            return Mono.just(new URL(recordUrl));
        } catch (MalformedURLException e) {
            throw new RuntimeException("Unable to parse url with location: " + recordUrl, e);
        }
    }
}
