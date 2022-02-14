package com.github.bottomlessarchive.loa.downloader.service.source.folder;

import com.github.bottomlessarchive.loa.downloader.service.document.DocumentLocationProcessor;
import com.github.bottomlessarchive.loa.downloader.service.source.configuration.DownloaderFolderSourceConfiguration;
import com.github.bottomlessarchive.loa.location.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.location.domain.link.UrlLink;
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

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "loa.downloader.source", havingValue = "folder")
public class DocumentFolderReader implements CommandLineRunner {

    private final DownloaderFolderSourceConfiguration downloaderFolderSourceConfiguration;
    private final DocumentSourceConfiguration documentSourceConfiguration;
    private final DocumentLocationProcessor documentLocationProcessor;

    @Override
    @SneakyThrows
    public void run(final String... args) {
        final Path sourceFolder = Path.of(downloaderFolderSourceConfiguration.getLocation());

        Files.list(sourceFolder)
                .forEach(path -> {
                    documentLocationProcessor.processDocumentLocation(buildDocumentSourceItem(path));

                    if (downloaderFolderSourceConfiguration.isShouldRemove()) {
                        try {
                            log.debug("Deleting file at: {}.", path);

                            Files.deleteIfExists(path);
                        } catch (final IOException e) {
                            log.error("Failed to delete source file!", e);
                        }
                    }
                });

        log.info("Finished processing the folder.");
    }

    @SneakyThrows
    private DocumentLocation buildDocumentSourceItem(final Path file) {
        return DocumentLocation.builder()
                .location(
                        UrlLink.builder()
                                .url(file.toUri().toURL())
                                .build()
                )
                .sourceName(documentSourceConfiguration.getName())
                .build();
    }
}
