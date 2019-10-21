package com.github.loa.downloader.command.batch.generator.commoncrawl.warc;

import org.davidmoten.io.extras.IOUtil;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This service is responsible for creating the locations to the WARC files for a batch of Common Crawl corpus.
 *
 * @see <a href="http://commoncrawl.org/">Common Crawl</a>
 */
@Service
public class WarcPathFactory {

    /**
     * Return the locations for the WARC files that belong to the provided Common Crawl crawl id.
     *
     * @param crawlId the id of the Common Crawl crawl to get the locations for
     * @return the locations of the WARC files
     */
    public List<String> newPaths(final String crawlId) {
        try (final BufferedReader downloadPathsReader = downloadPaths(crawlId)) {
            return downloadPathsReader.lines()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Unable to load WARC file paths.", e);
        }
    }

    private BufferedReader downloadPaths(final String pathsLocation) throws IOException {
        final InputStream unzippedPaths = IOUtil.gunzip(new URL("https://commoncrawl.s3.amazonaws.com/crawl-data/"
                + pathsLocation + "/warc.paths.gz").openStream());

        return new BufferedReader(new InputStreamReader(unzippedPaths, StandardCharsets.UTF_8));
    }
}
