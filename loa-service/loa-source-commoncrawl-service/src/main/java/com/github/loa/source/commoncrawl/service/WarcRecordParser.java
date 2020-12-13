package com.github.loa.source.commoncrawl.service;

import com.github.loa.location.domain.link.StringLink;
import com.github.loa.source.commoncrawl.service.webpage.domain.WebPage;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WarcRecordParser {

    public Flux<StringLink> parseLinksFromRecord(final WebPage warcRecord) {
        return Mono.fromSupplier(() -> Jsoup.parse(warcRecord.getContent(), warcRecord.getUrl()))
                .flatMapIterable(this::parseLinksFromDocument)
                .map(link -> StringLink.builder()
                        .link(link)
                        .build()
                );
    }

    private List<String> parseLinksFromDocument(final Document document) {
        return document.select("a").stream()
                .map(element -> element.absUrl("href").trim())
                .collect(Collectors.toList());
    }
}
