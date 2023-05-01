package com.github.bottomlessarchive.loa.stage.view.document.controller;

import com.github.bottomlessarchive.loa.stage.service.StageLocationFactory;
import com.github.bottomlessarchive.loa.stage.service.location.StageLocation;
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
import java.nio.file.Files;

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

    private final StageLocationFactory stageLocationFactory;

    @SneakyThrows
    @PostMapping("/document/{documentId}")
    public Mono<Void> persistDocument(@PathVariable final String documentId, @RequestPart("file") final Mono<FilePart> file) {
        final StageLocation documentLocation = stageLocationFactory.getStageLocation(documentId);

        return file.flatMap(p -> p.transferTo(documentLocation.location()));
    }

    @SneakyThrows
    @GetMapping("/document/{documentId}")
    public Mono<Void> serveDocument(@PathVariable final String documentId, final ServerHttpResponse response) {
        final StageLocation documentLocation = stageLocationFactory.getStageLocation(documentId);

        return ((ZeroCopyHttpOutputMessage) response)
                .writeWith(documentLocation.location(), 0, Files.size(documentLocation.location()))
                .then(deleteIfExists(documentLocation));
    }

    @DeleteMapping("/document/{documentId}")
    public Mono<Void> deleteDocument(@PathVariable final String documentId) {
        final StageLocation documentLocation = stageLocationFactory.getStageLocation(documentId);

        return deleteIfExists(documentLocation);
    }

    private Mono<Void> deleteIfExists(final StageLocation file) {
        return Mono.fromRunnable(() -> {
            try {
                Files.deleteIfExists(file.location());
            } catch (IOException e) {
                log.error("Failed to delete staging file at: {}!", file.location());
            }
        });
    }
}
