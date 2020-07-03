package com.github.loa.url.service;

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
        } catch (final MalformedURLException e) {
            log.debug("Unable to parse url with location: {}", recordUrl, e);

            return Mono.empty();
        }
    }
}
