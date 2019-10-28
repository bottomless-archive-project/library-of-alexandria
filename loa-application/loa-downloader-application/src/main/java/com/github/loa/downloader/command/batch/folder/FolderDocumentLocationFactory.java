package com.github.loa.downloader.command.batch.folder;

import com.github.loa.downloader.command.batch.DocumentLocationFactory;
import com.github.loa.source.folder.configuration.FolderDocumentSourceConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "loa.source.type", havingValue = "folder")
public class FolderDocumentLocationFactory implements DocumentLocationFactory {

    private final FolderDocumentSourceConfiguration folderDocumentSourceConfiguration;

    @Override
    public Flux<URL> streamLocations() {
        try (Stream<Path> paths = Files.walk(Paths.get(folderDocumentSourceConfiguration.getLocation()))) {
            return Flux.fromStream(paths)
                    .map(path -> {
                        try {
                            return path.toUri().toURL();
                        } catch (MalformedURLException e) {
                            throw new RuntimeException("Unable to convert file!", e);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException("Unable to walk folder!", e);
        }
    }
}
