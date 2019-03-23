package com.github.loa.source.service.commoncrawl;

import com.github.loa.source.configuration.commoncrawl.CommonCrawlDocumentSourceConfiguration;
import com.github.loa.source.service.DocumentSourceProvider;
import com.morethanheroic.warc.service.WarcRecordStreamFactory;
import com.morethanheroic.warc.service.content.response.domain.ResponseContentBlock;
import com.morethanheroic.warc.service.record.domain.WarcRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.davidmoten.io.extras.IOUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommonCrawlDocumentSourceProvider implements DocumentSourceProvider {

    private final CommonCrawlDocumentSourceConfiguration commonCrawlDocumentSourceConfiguration;

    @Override
    public Stream<URL> stream() {
        try {
            log.info("Downloading paths from crawl id location: {}.",
                    commonCrawlDocumentSourceConfiguration.getCrawlId());

            try (final BufferedReader reader = downloadPaths(commonCrawlDocumentSourceConfiguration.getCrawlId())) {
                final List<String> crawlLocations = reader.lines().collect(Collectors.toList());

                return crawlLocations.stream()
                        .skip(commonCrawlDocumentSourceConfiguration.getWarcId())
                        .map(warcLocation -> {
                            log.info("Starting to crawl WARC location with id: {}, location: {}.",
                                    crawlLocations.indexOf(warcLocation), warcLocation);

                            try {
                                return handleWarcFile(warcLocation);
                            } catch (Exception e) {
                                log.error("Unable to crawl WARC location: " + warcLocation + "!", e);

                                return Stream.<URL>empty();
                            }
                        })
                        .flatMap(Function.identity());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private BufferedReader downloadPaths(final String pathsLocation) throws IOException {
        final InputStream unzippedPaths = IOUtil.gunzip(new URL("https://commoncrawl.s3.amazonaws.com/crawl-data/"
                + pathsLocation + "/warc.paths.gz").openStream());

        return new BufferedReader(new InputStreamReader(unzippedPaths, StandardCharsets.UTF_8));
    }

    private Stream<URL> handleWarcFile(final String warcLocation) throws IOException {
        return WarcRecordStreamFactory.streamOf(new URL(warcLocation))
                .filter(WarcRecord::isResponse)
                .map(warcRecord -> {
                    final String warcRecordUrl = warcRecord.getHeader("WARC-Target-URI");

                    final Document document = Jsoup.parse(((ResponseContentBlock) warcRecord.getWarcContentBlock())
                            .getPayloadAsString(), warcRecordUrl);

                    return document.select("a").stream()
                            .map(element -> element.attr("abs:href"))
                            .map(url -> {
                                log.debug("Parsed url from war file: {}.", url);

                                try {
                                    return Optional.of(new URL(url));
                                } catch (MalformedURLException e) {
                                    e.printStackTrace();

                                    return Optional.<URL>empty();
                                }
                            });
                })
                .flatMap(Function.identity())
                .filter(Optional::isPresent)
                .map(Optional::get);
    }
}
