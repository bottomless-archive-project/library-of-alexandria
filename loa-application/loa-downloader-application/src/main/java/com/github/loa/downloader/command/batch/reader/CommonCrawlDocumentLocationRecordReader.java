package com.github.loa.downloader.command.batch.reader;

import com.github.loa.source.configuration.commoncrawl.CommonCrawlDocumentSourceConfiguration;
import com.morethanheroic.warc.service.WarcRecordStreamFactory;
import com.morethanheroic.warc.service.content.response.domain.ResponseContentBlock;
import com.morethanheroic.warc.service.record.domain.WarcRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.davidmoten.io.extras.IOUtil;
import org.easybatch.core.reader.RecordReader;
import org.easybatch.core.record.Header;
import org.easybatch.core.record.Record;
import org.easybatch.core.record.StringRecord;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "loa.source.type", havingValue = "common-crawl")
public class CommonCrawlDocumentLocationRecordReader implements RecordReader {

    private final CommonCrawlDocumentSourceConfiguration commonCrawlDocumentSourceConfiguration;

    private int processedWarcFiles;
    private long line = 0;
    private List<String> crawlLocations = new ArrayList<>();
    private List<URL> availableUrls = new ArrayList<>();

    @Override
    public void open() throws Exception {
        try (BufferedReader downloadPathsReader = downloadPaths(commonCrawlDocumentSourceConfiguration.getCrawlId())) {
            crawlLocations = downloadPathsReader.lines()
                    .skip(commonCrawlDocumentSourceConfiguration.getWarcId())
                    .collect(Collectors.toList());
        }
    }

    @Override
    public Record readRecord() throws Exception {
        if (availableUrls.size() == 0) {
            log.info("Starting the processing of WARC file with ID: " + (
                    commonCrawlDocumentSourceConfiguration.getWarcId() + processedWarcFiles));

            availableUrls = handleWarcFile(crawlLocations.remove(0))
                    .collect(Collectors.toList());

            log.info("Finished the processing of WARC file with ID: " + (
                    commonCrawlDocumentSourceConfiguration.getWarcId() + processedWarcFiles));

            processedWarcFiles++;
        }

        return new StringRecord(new Header(line++, "file", new Date()), availableUrls.remove(0).toString());
    }

    @Override
    public void close() {

    }

    private BufferedReader downloadPaths(final String pathsLocation) throws IOException {
        final InputStream unzippedPaths = IOUtil.gunzip(new URL("https://commoncrawl.s3.amazonaws.com/crawl-data/"
                + pathsLocation + "/warc.paths.gz").openStream());

        return new BufferedReader(new InputStreamReader(unzippedPaths, StandardCharsets.UTF_8));
    }

    private Stream<URL> handleWarcFile(final String warcLocation) throws IOException {
        return WarcRecordStreamFactory.streamOf(new URL("https://commoncrawl.s3.amazonaws.com/" + warcLocation))
                .filter(WarcRecord::isResponse)
                .map(warcRecord -> {
                    try {
                        final String warcRecordUrl = warcRecord.getHeader("WARC-Target-URI");

                        final Document document = Jsoup.parse(((ResponseContentBlock) warcRecord.getWarcContentBlock())
                                .getPayloadAsString(), warcRecordUrl);

                        return document.select("a").stream()
                                .map(element -> element.attr("abs:href"))
                                .map(url -> {
                                    log.debug("Parsed url from war file: {}.", url);

                                    if (url.isEmpty()) {
                                        return Optional.<URL>empty();
                                    }

                                    try {
                                        return Optional.of(new URL(url));
                                    } catch (MalformedURLException e) {
                                        e.printStackTrace();

                                        return Optional.<URL>empty();
                                    }
                                });
                    } catch (Exception e) {
                        log.info("Failed to parse url content!");

                        return Stream.of(Optional.<URL>empty());
                    }
                })
                .flatMap(Function.identity())
                .filter(Optional::isPresent)
                .map(Optional::get);
    }
}
