package com.github.loa.source.file.service.location;

import com.github.loa.source.file.service.FileSourceFactory;
import com.github.loa.source.service.DocumentLocationFactory;
import com.github.loa.url.service.URLConverter;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Path;

@RequiredArgsConstructor
public class FileDocumentLocationFactory implements DocumentLocationFactory {

    private final FileSourceFactory fileSourceFactory;
    private final URLConverter urlConverter;
    private final Path filePath;

    @Override
    public Flux<URL> streamLocations() {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(
                fileSourceFactory.newInputStream(filePath)));

        return Flux.fromStream(reader.lines())
                .flatMap(urlConverter::convert);
    }
}
