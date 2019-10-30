package com.github.loa.downloader.command.batch.commoncrawl.warc;

import com.morethanheroic.warc.service.content.response.domain.ResponseContentBlock;
import com.morethanheroic.warc.service.record.domain.WarcRecord;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class WarcRecordParser {

    public Flux<String> parseUrlsFromRecord(final WarcRecord warcRecord) {
        // All information should be read from the stream before doing parallel processing!
        final String warcRecordUrl = warcRecord.getHeader("WARC-Target-URI");
        final String contentString = ((ResponseContentBlock) warcRecord.getWarcContentBlock()).getPayloadAsString();

        try {
            final Document document = Jsoup.parse(contentString, warcRecordUrl);

            return Flux.fromStream(() -> document.select("a").stream()
                    .map(element -> element.attr("abs:href"))
                    .filter(url -> !url.isEmpty()));
        } catch (Exception e) {
            return Flux.empty();
        }
    }
}
