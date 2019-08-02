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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "loa.source.type", havingValue = "common-crawl")
public class CommonCrawlDocumentGenerator implements Generator<String> {

    private final CommonCrawlDocumentSourceConfiguration commonCrawlDocumentSourceConfiguration;
    private final WarcPathFactory warcPathFactory;

    private int processedWarcFiles;
    private List<String> crawlLocations = new ArrayList<>();
    private List<String> availableUrls = Collections.synchronizedList(new ArrayList<>());
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Override
    public void open() {
        crawlLocations = warcPathFactory.newPaths(commonCrawlDocumentSourceConfiguration.getCrawlId()).stream()
                .skip(commonCrawlDocumentSourceConfiguration.getWarcId())
                .collect(Collectors.toList());
    }

    @Override
    public Optional<String> generate() {
        if (availableUrls.size() == 0) {
            log.info("Starting the processing of WARC file with ID: " + (
                    commonCrawlDocumentSourceConfiguration.getWarcId() + processedWarcFiles));

            handleWarcFile(crawlLocations.remove(0));

            log.info("Finished the processing of WARC file with ID: " + (
                    commonCrawlDocumentSourceConfiguration.getWarcId() + processedWarcFiles));

            processedWarcFiles++;
        }

        return Optional.of(availableUrls.remove(0));
    }

    private void handleWarcFile(final String warcLocation) {
        try {
            WarcRecordStreamFactory.streamOf(buildWarcLocation(warcLocation))
                    .filter(WarcRecord::isResponse)
                    .forEach(this::handleWarcRecord);
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

    private void handleWarcRecord(final WarcRecord warcRecord) {
        try {
            // All information should be read from the stream before doing parallel processing!
            final String warcRecordUrl = warcRecord.getHeader("WARC-Target-URI");
            final String contentString = ((ResponseContentBlock) warcRecord.getWarcContentBlock()).getPayloadAsString();

            executorService.submit(() -> {
                final Document document = Jsoup.parse(contentString, warcRecordUrl);

                final List<String> urlsOnPage = document.select("a").stream()
                        .map(element -> element.attr("abs:href"))
                        .filter(url -> !url.isEmpty())
                        .collect(Collectors.toList());

                final int beforeUrls = availableUrls.size();

                availableUrls.addAll(urlsOnPage);

                final int afterUrls = availableUrls.size();

                if ((afterUrls / 25000) - (beforeUrls / 25000) > 0) {
                    log.info("Collected " + afterUrls + " urls!");
                }
            });
        } catch (Exception e) {
            log.debug("Failed to parse url content!");
        }
    }
}
