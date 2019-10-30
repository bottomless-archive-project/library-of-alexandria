package com.github.loa.downloader.command.batch.file;

import com.github.loa.downloader.command.batch.DocumentLocationFactory;
import com.github.loa.downloader.service.url.URLConverter;
import com.github.loa.source.file.configuration.FileDocumentSourceConfiguration;
import com.github.loa.source.file.service.FileSourceFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "loa.source.type", havingValue = "file")
public class FileDocumentLocationFactory implements DocumentLocationFactory {

    private final FileDocumentSourceConfiguration fileDocumentSourceConfiguration;
    private final FileSourceFactory fileSourceFactory;
    private final URLConverter urlConverter;

    @Override
    public Flux<URL> streamLocations() {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(fileSourceFactory.newInputStream(
                fileDocumentSourceConfiguration.getLocation())));

        return Flux.fromStream(reader.lines())
                .flatMap(urlConverter::convert);
    }
}
