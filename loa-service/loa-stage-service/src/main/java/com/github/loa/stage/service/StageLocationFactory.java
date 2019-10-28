package com.github.loa.stage.service;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.domain.DocumentType;
import com.github.loa.stage.configuration.StageConfigurationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.File;

/**
 * A factory that creates {@link java.io.File} instances in the staging area. These files could be used to save the
 * document for a short time for pre-processing before moving it into the vault.
 */
@Service
@RequiredArgsConstructor
public class StageLocationFactory {

    private final StageConfigurationProperties stageConfigurationProperties;

    /**
     * Return a file in the staging area that's uniquely generated for the provided document.
     *
     * @param documentEntity the document that we need to create the location for
     * @return the location created in the staging area
     */
    public Mono<File> getLocation(final DocumentEntity documentEntity) {
        return getLocation(documentEntity.getId(), documentEntity.getType());
    }

    /**
     * Return a file in the staging area that's uniquely generated for the provided document id.
     *
     * @param documentId   the document's id that we need to create the location for
     * @param documentType the type of the document that we need to create the location for
     * @return the location created in the staging area
     */
    public Mono<File> getLocation(final String documentId, final DocumentType documentType) {
        return Mono.just(new File(stageConfigurationProperties.getLocation(), documentId + "."
                + documentType.getFileExtension()));
    }
}
