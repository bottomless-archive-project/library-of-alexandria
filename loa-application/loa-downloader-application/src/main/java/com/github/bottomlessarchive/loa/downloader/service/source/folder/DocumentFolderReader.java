package com.github.bottomlessarchive.loa.downloader.service.source.folder;

import com.github.bottomlessarchive.loa.downloader.service.document.DocumentLocationProcessorWrapper;
import com.github.bottomlessarchive.loa.downloader.service.source.configuration.DownloaderFolderSourceConfiguration;
import com.github.bottomlessarchive.loa.location.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.source.configuration.DocumentSourceConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "loa.downloader.source", havingValue = "folder")
public class DocumentFolderReader implements CommandLineRunner {

    private final DownloaderFolderSourceConfiguration downloaderFolderSourceConfiguration;
    private final DocumentSourceConfiguration documentSourceConfiguration;
    private final DocumentLocationProcessorWrapper documentLocationProcessorWrapper;

    @Override
    @SneakyThrows
    public void run(final String... args) {
        final Path sourceFolder = Path.of(downloaderFolderSourceConfiguration.getLocation());

        try (Stream<Path> files = Files.list(sourceFolder)) {
            files.forEach(path -> documentLocationProcessorWrapper.processDocumentLocation(buildDocumentSourceItem(path), () -> {
                                if (downloaderFolderSourceConfiguration.isShouldRemove()) {
                                    try {
                                        log.debug("Deleting file at: {}.", path);

                                        Files.deleteIfExists(path);
                                    } catch (final IOException e) {
                                        log.error("Failed to delete source file!", e);
                                    }
                                }
                            }
                    )
            );
        }

        log.info("Finished processing the folder.");
    }

    @SneakyThrows
    private DocumentLocation buildDocumentSourceItem(final Path file) {
        log.debug("Starting to parse document at location: {}.", file);

        return DocumentLocation.builder()
                .location(file.toUri().toURL())
                .sourceName(documentSourceConfiguration.getName())
                .build();
    }
}
