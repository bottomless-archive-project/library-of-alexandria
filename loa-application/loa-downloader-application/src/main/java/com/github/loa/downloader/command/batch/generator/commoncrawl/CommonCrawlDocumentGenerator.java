package com.github.loa.downloader.command.batch.generator.commoncrawl;

import com.github.loa.downloader.command.batch.generator.commoncrawl.warc.WarcPathFactory;
import com.github.loa.source.configuration.commoncrawl.CommonCrawlDocumentSourceConfiguration;
import com.morethanheroic.taskforce.generator.Generator;
import com.morethanheroic.warc.service.WarcRecordStreamFactory;
import com.morethanheroic.warc.service.content.response.domain.ResponseContentBlock;
import com.morethanheroic.warc.service.record.domain.WarcRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "loa.source.type", havingValue = "common-crawl")
public class CommonCrawlDocumentGenerator implements Generator<String> {

    private final CommonCrawlDocumentSourceConfiguration commonCrawlDocumentSourceConfiguration;
    private final WarcPathFactory warcPathFactory;

    private int processedWarcFiles;
    private List<String> crawlLocations = new ArrayList<>();
    private List<Optional<String>> availableUrls = new ArrayList<>();

    @Override
    public void open() {
        crawlLocations = warcPathFactory.newPaths(commonCrawlDocumentSourceConfiguration.getCrawlId()).stream()
                .skip(commonCrawlDocumentSourceConfiguration.getWarcId())
                .collect(Collectors.toList());
    }

    //TODO: Clean this up!
    @Override
    public Optional<String> generate() {
        if (availableUrls.size() == 0) {
            log.info("Starting the processing of WARC file with ID: " + (
                    commonCrawlDocumentSourceConfiguration.getWarcId() + processedWarcFiles));

            availableUrls = handleWarcFile(crawlLocations.remove(0))
                    .filter(Optional::isPresent)
                    .collect(Collectors.toList());

            log.info("Finished the processing of WARC file with ID: " + (
                    commonCrawlDocumentSourceConfiguration.getWarcId() + processedWarcFiles));

            processedWarcFiles++;
        }

        return availableUrls.remove(0);
    }

    private Stream<Optional<String>> handleWarcFile(final String warcLocation) {
        try {
            return WarcRecordStreamFactory.streamOf(buildWarcLocation(warcLocation))
                    .filter(WarcRecord::isResponse)
                    .map(warcRecord -> {
                        try {
                            final String warcRecordUrl = warcRecord.getHeader("WARC-Target-URI");

                            final Document document = Jsoup.parse(((ResponseContentBlock) warcRecord.getWarcContentBlock())
                                    .getPayloadAsString(), warcRecordUrl);

                            return document.select("a").stream()
                                    .map(element -> Optional.of(element.attr("abs:href")));
                        } catch (Exception e) {
                            log.info("Failed to parse url content!");

                            return Stream.of(Optional.<String>empty());
                        }
                    })
                    .flatMap(Function.identity());
        } catch (Exception e) {
            throw new RuntimeException("Unable to load urls from WARC location: " + warcLocation, e);
        }
    }

    private URL buildWarcLocation(final String warcLocation) {
        try {
            return new URL("https://commoncrawl.s3.amazonaws.com/" + warcLocation);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Unable to build WARC location!", e);
        }
    }
}
