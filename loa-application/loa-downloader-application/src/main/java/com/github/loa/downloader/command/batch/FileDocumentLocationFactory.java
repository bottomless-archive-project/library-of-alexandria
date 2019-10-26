package com.github.loa.downloader.command.batch;

import com.github.loa.source.configuration.file.FileDocumentSourceConfiguration;
import com.github.loa.source.service.file.FileSourceFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "loa.source.type", havingValue = "file")
public class FileDocumentLocationFactory implements DocumentLocationFactory {

    private final FileDocumentSourceConfiguration fileDocumentSourceConfiguration;
    private final FileSourceFactory fileSourceFactory;

    @Override
    public Flux<String> streamLocations() {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(fileSourceFactory.newInputStream(
                fileDocumentSourceConfiguration.getLocation())));

        return Flux.fromStream(reader.lines());
    }
}
