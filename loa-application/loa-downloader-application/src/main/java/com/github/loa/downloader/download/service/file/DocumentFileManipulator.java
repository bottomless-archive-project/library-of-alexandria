package com.github.loa.downloader.download.service.file;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.domain.DocumentType;
import com.github.loa.stage.service.StageLocationFactory;
import com.github.loa.vault.client.service.VaultClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.File;

/**
 * This service is responsible for the manipulating of document files. For example moving them to the vault or removing
 * them if necessary.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentFileManipulator {

    private final StageLocationFactory stageLocationFactory;
    private final VaultClientService vaultClientService;

    /**
     * Move a document's file from the staging area to the vault. The document's metadata is not updated by this method!
     *
     * @param documentEntity the document's id that we want to move to the vault
     */
    public Mono<Void> moveToVault(final DocumentEntity documentEntity) {
        log.info("Moving document to vault {}!", documentEntity);

        return stageLocationFactory.getLocation(documentEntity)
                .flatMap(documentLocation -> vaultClientService.archiveDocument(documentEntity, documentLocation)
                        .thenReturn(documentLocation))
                .map(documentLocation -> cleanup(documentEntity))
                .then();
    }

    /**
     * Clean up after a document's download by removing all the staging information belonging to that document.
     *
     * @param documentEntity the document to clean up after
     */
    public Mono<Void> cleanup(final DocumentEntity documentEntity) {
        return cleanup(documentEntity.getId(), documentEntity.getType());
    }

    public Mono<Void> cleanup(final String documentId, final DocumentType documentType) {
        log.debug("Cleaning up staging for document {}.", documentId);

        return stageLocationFactory.getLocation(documentId, documentType)
                .map(File::delete)
                .then();
    }
}
