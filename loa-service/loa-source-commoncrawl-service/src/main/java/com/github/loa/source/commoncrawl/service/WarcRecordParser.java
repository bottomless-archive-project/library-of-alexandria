package com.github.loa.source.commoncrawl.service;

import com.github.loa.source.commoncrawl.service.webpage.domain.WebPage;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class WarcRecordParser {

    public Flux<String> parseUrlsFromRecord(final WebPage warcRecord) {
        return Mono.fromSupplier(() -> Jsoup.parse(warcRecord.getContent(), warcRecord.getUrl()))
                .flatMapIterable(document -> document.select("a"))
                .map(element -> element.absUrl("href"))
                .filter(url -> !url.isEmpty());
    }
}
