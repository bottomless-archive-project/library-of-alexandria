package com.github.bottomlessarchive.loa.stage.service;

import com.github.bottomlessarchive.loa.stage.configuration.StageConfigurationProperties;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentType;
import com.github.bottomlessarchive.loa.stage.service.domain.StageLocation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.nio.file.Path;

/**
 * A factory that creates {@link java.io.File} instances in the staging area. These files could be used to save the
 * document for a short time for pre-processing before moving it into the vault.
 */
@Service
@RequiredArgsConstructor
public class StageLocationFactory {

    private final StageConfigurationProperties stageConfigurationProperties;

    /**
     * Return a path in the staging area that's uniquely generated for the provided document id.
     *
     * @param documentId   the document's id that we need to create the location for
     * @param documentType the type of the document that we need to create the location for
     * @return the location created in the staging area
     */
    public Mono<StageLocation> getLocation(final String documentId, final DocumentType documentType) {
        return Mono.fromSupplier(() -> Path.of(stageConfigurationProperties.location(), buildFileName(documentId, documentType)))
                .map(path -> StageLocation.builder()
                        .path(path)
                        .build()
                );
    }

    private String buildFileName(final String documentId, final DocumentType documentType) {
        return documentId + '.' + documentType.getFileExtension();
    }
}
