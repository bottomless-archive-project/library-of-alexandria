package com.github.bottomlessarchive.loa.stage.view.document.controller;

import com.github.bottomlessarchive.loa.stage.configuration.StagingConfigurationProperties;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.ZeroCopyHttpOutputMessage;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequiredArgsConstructor
public class StageDocumentController {

    private final StagingConfigurationProperties stagingConfigurationProperties;

    @SneakyThrows
    @PostMapping("/document/{documentId}")
    public Mono<Void> persistDocument(@PathVariable final String documentId,
            @RequestPart("file") final Mono<FilePart> file) {
        return file.flatMap(p -> p.transferTo(Path.of(stagingConfigurationProperties.path()).resolve(documentId)))
                .then();
    }

    @GetMapping("/document/{documentId}")
    public Mono<Void> serveDocument(@PathVariable final String documentId, ServerHttpResponse response) throws IOException {
        ZeroCopyHttpOutputMessage zeroCopyResponse = (ZeroCopyHttpOutputMessage) response;

        Path file = Path.of(stagingConfigurationProperties.path()).resolve(documentId);

        return zeroCopyResponse.writeWith(file, 0, Files.size(file))
                .then(
                        Mono.fromRunnable(() -> {
                            try {
                                Files.deleteIfExists(file);
                            } catch (IOException e) {
                                // Do nothing
                            }
                        })
                );
    }
}
