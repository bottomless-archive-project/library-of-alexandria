package com.github.bottomlessarchive.loa.stage.view.document.controller;

import blue.strategic.parquet.ParquetWriter;
import com.github.bottomlessarchive.loa.batch.service.domain.BatchEntity;
import com.github.bottomlessarchive.loa.batch.service.entity.factory.BatchEntityFactory;
import com.github.bottomlessarchive.loa.stage.configuration.StagingConfigurationProperties;
import com.github.bottomlessarchive.loa.stage.service.StageLocationFactory;
import com.github.bottomlessarchive.loa.stage.service.location.StageLocation;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.parquet.schema.LogicalTypeAnnotation;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.Types;
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
import java.nio.file.Path;

import static org.apache.parquet.schema.PrimitiveType.PrimitiveTypeName.BINARY;

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

    private final BatchEntityFactory batchEntityFactory;
    private final StageLocationFactory stageLocationFactory;
    private final StagingConfigurationProperties stagingConfigurationProperties;

    //TODO: Move this to somewhere else
    private final static MessageType DOCUMENT_PARQUET_SCHEMA = new MessageType("documents",
            Types.required(BINARY)
                    .as(LogicalTypeAnnotation.stringType())
                    .named("documentId"),
            Types.required(BINARY)
                    .named("documentContent")
    );

    @SneakyThrows
    @PostMapping("/document/{documentId}")
    public Mono<Void> persistDocument(@PathVariable final String documentId, @RequestPart("file") final Mono<FilePart> file) {
        final StageLocation documentLocation = stageLocationFactory.getStageLocation(documentId);

        return file.flatMap(p -> p.transferTo(documentLocation.location()));
    }

    @GetMapping("/document/next-batch")
    public Mono<Void> nextBatch() {
        //TODO: Create the parquet file and return it

        //TODO: Move this logic into the service layer after the parquet saving is done
        final BatchEntity nextBatchEntity = batchEntityFactory.getNextBatch()
                .orElseGet(batchEntityFactory::newBatchEntity);

        final Path nextBatchPath = stagingConfigurationProperties.location()
                .resolve(nextBatchEntity.id() + ".parquet");

        try (ParquetWriter parquetWriter = ParquetWriter.writeFile()) {

        }
    }

    //TODO: Replace these with the batch based downloads
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
    //TODO: End

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
