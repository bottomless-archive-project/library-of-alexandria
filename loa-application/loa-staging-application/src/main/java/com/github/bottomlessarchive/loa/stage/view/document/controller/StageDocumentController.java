package com.github.bottomlessarchive.loa.stage.view.document.controller;

import com.github.bottomlessarchive.loa.file.FileManipulatorService;
import com.github.bottomlessarchive.loa.stage.configuration.StagingConfigurationProperties;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ZeroCopyHttpOutputMessage;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Path;

/**
 * This class is responsible to serve the web-related functionality of the Staging Application.
 * <p>
 * It uses webflux instead of the servlet-based webservers because a zero-copy should be done to serve files optimally and the servlet
 * stack doesn't support zero-copy yet.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Zero-copy">https://en.wikipedia.org/wiki/Zero-copy</a>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class StageDocumentController {

    private final FileManipulatorService fileManipulatorService;
    private final StagingConfigurationProperties stagingConfigurationProperties;

    @SneakyThrows
    @PostMapping("/document/{documentId}")
    public Mono<Void> persistDocument(@PathVariable final String documentId, @RequestPart("file") final Mono<FilePart> file) {
        final Path documentLocation = fileManipulatorService.newFile(stagingConfigurationProperties.location(), documentId);

        return file.flatMap(p -> p.transferTo(documentLocation)
                .then(p.delete())
        );
    }

    @SneakyThrows
    @GetMapping("/document/{documentId}")
    public Mono<Void> serveDocument(@PathVariable final String documentId, final ServerHttpResponse response) {
        final Path documentLocation = fileManipulatorService.newFile(stagingConfigurationProperties.location(), documentId);

        return ((ZeroCopyHttpOutputMessage) response)
                .writeWith(documentLocation, 0, fileManipulatorService.size(documentLocation))
                .then(deleteIfExists(documentLocation));
    }

    @DeleteMapping("/document/{documentId}")
    public Mono<Void> deleteDocument(@PathVariable final String documentId) {
        final Path documentLocation = fileManipulatorService.newFile(stagingConfigurationProperties.location(), documentId);

        return deleteIfExists(documentLocation);
    }

    private Mono<Void> deleteIfExists(final Path file) {
        return Mono.fromRunnable(() -> {
            try {
                fileManipulatorService.deleteIfExists(file);
            } catch (IOException e) {
                log.error("Failed to delete staging file at: {}!", file);
            }
        });
    }
}
