package com.github.bottomlessarchive.loa.administrator.command.recollect;

import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.bottomlessarchive.loa.stage.service.StageLocationFactory;
import com.github.bottomlessarchive.loa.stage.service.domain.StageLocation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty("recollect-corrupt-documents")
public class RecollectCorruptDocumentsCommand implements CommandLineRunner {

    private final StageLocationFactory stageLocationFactory;
    private final DocumentFileValidator
    private final DocumentEntityFactory documentEntityFactory;

    @Override
    public void run(final String... args) {
        log.info("Started to run the recollect corrupt documents command!");

        documentEntityFactory.getDocumentEntities()
                .filter(DocumentEntity::isCorrupt)
                .flatMap(this::recollectCorruptDocument)
                .subscribe();
    }

    private Mono<Void> recollectCorruptDocument(final DocumentEntity documentEntity) {
        log.info("Recollecting document entity: {}.", documentEntity);

        final String documentRecrawlId = UUID.randomUUID().toString();

        return Flux.fromIterable(documentEntity.getSourceLocations())
                .map(sourceLocation -> {
                    try {
                        return new URL(sourceLocation);
                    } catch (MalformedURLException e) {
                        throw new IllegalStateException("Illegal URL: " + sourceLocation + "!");
                    }
                })
                .flatMap(sourceLocation -> stageLocationFactory.getLocation(documentRecrawlId, documentEntity.getType())
                        .flatMap(stageFileLocation -> acquireFile(sourceLocation, stageFileLocation))
                        .flatMap(documentFileLocation -> documentFileValidator.isValidDocument(documentId.toString(), documentType)
                                .filter(validationResult -> !validationResult)
                                .flatMap(validationResult -> documentFileLocation.cleanup())
                                .thenReturn(documentFileLocation)
                        )
                        .filterWhen(StageLocation::exists)
                        // TODO: Send it here!
                        .flatMap(this::cleanup)
                        .onErrorResume(error -> {
                            if (log.isDebugEnabled()) {
                                log.debug("Error downloading a document: {}!", error.getMessage());
                            }

                            return Mono.empty();
                        })
                );
    }

    private Mono<StageLocation> acquireFile(final URL documentLocation, final StageLocation stageLocation) {
        return fileCollector.acquireFile(documentLocation, stageLocation.getPath())
                .thenReturn(stageLocation);
    }

    private Mono<Void> cleanup(final DocumentArchivingContext documentArchivingContext) {
        return stageLocationFactory.getLocation(documentArchivingContext.getId().toString(), documentArchivingContext.getType())
                .flatMap(StageLocation::cleanup);
    }
}
