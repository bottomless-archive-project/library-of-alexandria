package com.github.loa.source.cc.service;

import com.github.loa.url.service.URLConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.net.URL;

@Slf4j
@Service
@RequiredArgsConstructor
public class WarcDownloader {

    private final URLConverter urlConverter;

    public Mono<URL> downloadWarcFile(final String warcLocation) {
        return urlConverter.convertOrThrow("https://commoncrawl.s3.amazonaws.com/" + warcLocation)
                .doOnNext(warcUrl -> log.debug("Started to download warc file at location: {}!", warcUrl));
    }
}
