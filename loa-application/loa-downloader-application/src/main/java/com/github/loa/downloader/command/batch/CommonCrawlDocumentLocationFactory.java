package com.github.loa.downloader.command.batch;

import com.github.loa.downloader.command.batch.generator.commoncrawl.warc.WarcPathFactory;
import com.github.loa.source.configuration.commoncrawl.CommonCrawlDocumentSourceConfiguration;
import com.morethanheroic.warc.service.WarcRecordStreamFactory;
import com.morethanheroic.warc.service.content.response.domain.ResponseContentBlock;
import com.morethanheroic.warc.service.record.domain.WarcRecord;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "loa.source.type", havingValue = "common-crawl")
public class CommonCrawlDocumentLocationFactory implements DocumentLocationFactory {

    private final WarcPathFactory warcPathFactory;
    private final CommonCrawlDocumentSourceConfiguration commonCrawlDocumentSourceConfiguration;

    @Override
    public Flux<String> streamLocations() {
        final List<String> paths = warcPathFactory.newPaths(commonCrawlDocumentSourceConfiguration.getCrawlId()).stream()
                .skip(commonCrawlDocumentSourceConfiguration.getWarcId())
                .collect(Collectors.toList());

        return Flux.fromIterable(paths)
                .flatMap(warcLocation -> Flux.fromStream(() -> buildWarcRecordStream(warcLocation)))
                .flatMap(this::handleWarcRecord);
    }

    private Stream<WarcRecord> buildWarcRecordStream(final String warcLocation) {
        try {
            return WarcRecordStreamFactory.streamOf(buildWarcLocation(warcLocation))
                    .filter(WarcRecord::isResponse);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private URL buildWarcLocation(final String warcLocation) {
        try {
            return new URL("https://commoncrawl.s3.amazonaws.com/" + warcLocation);
        } catch (final MalformedURLException e) {
            throw new RuntimeException("Unable to build WARC location!", e);
        }
    }

    private Flux<String> handleWarcRecord(final WarcRecord warcRecord) {
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
