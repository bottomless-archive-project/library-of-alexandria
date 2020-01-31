package com.github.loa.source.cc.service;

import com.github.bottomlessarchive.warc.service.content.response.domain.ResponseContentBlock;
import com.github.bottomlessarchive.warc.service.record.domain.WarcRecord;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class WarcRecordParser {

    public Flux<String> parseUrlsFromRecord(final WarcRecord warcRecord) {
        final String warcRecordUrl = warcRecord.getHeader("WARC-Target-URI");
        final String contentString = ((ResponseContentBlock) warcRecord.getWarcContentBlock()).getPayloadAsString();

        return Mono.fromSupplier(() -> parseDocument(warcRecordUrl, contentString))
                .flatMapIterable(document -> document.select("a"))
                .map(element -> element.absUrl("href"))
                .filter(url -> !url.isEmpty());
    }

    private Document parseDocument(final String warcRecordUrl, final String contentString) {
        try {
            return Jsoup.parse(contentString, warcRecordUrl);
        } catch (Exception e) {
            return null;
        }
    }
}
