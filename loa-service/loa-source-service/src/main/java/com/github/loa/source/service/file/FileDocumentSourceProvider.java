package com.github.loa.source.service.file;

import com.github.loa.source.configuration.file.FileDocumentSourceConfiguration;
import com.github.loa.source.service.DocumentSourceProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "loa.source.type", havingValue = "file")
public class FileDocumentSourceProvider implements DocumentSourceProvider {

    private final FileDocumentSourceConfiguration fileDocumentSourceConfiguration;
    private final FileSourceFactory fileSourceFactory;

    //TODO: This is a crappy (non-reactive and badly implemented) solution!
    @Override
    public Stream<URL> stream() {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(
                fileSourceFactory.newInputStream(fileDocumentSourceConfiguration.getLocation())));

        return reader.lines()
                .map(this::buildUrl)
                .filter(Objects::nonNull);
    }

    //Not using optional for performance reasons
    private URL buildUrl(final String location) {
        try {
            return new URL(location);
        } catch (MalformedURLException e) {
            log.warn("Unable to parse url with location: " + location, e);

            return null;
        }
    }
}
