package com.github.loa.downloader.service.file;

import com.github.loa.vault.client.service.domain.ArchivingContext;
import com.github.loa.vault.client.service.VaultClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.nio.file.Path;
import java.time.Duration;

/**
 * This service is responsible for the manipulating of document files. For example moving them to the vault or removing
 * them if necessary.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentFileManipulator {

    private final VaultClientService vaultClientService;

    /**
     * Move a document's file from the staging area to the vault. The document's metadata is not updated by this method!
     */
    public Mono<Void> moveToVault(final ArchivingContext archivingContext) {
        log.info("Moving document to vault {}!", archivingContext.getContents().getFileName());

        return Mono.just(archivingContext)
                .flatMap(documentLocation -> vaultClientService.archiveDocument(documentLocation)
                        .thenReturn(documentLocation))
                .map(ArchivingContext::getContents)
                .flatMap(this::cleanup)
                .onErrorResume(error -> {
                    log.error("Error archiving a document: {}!", error.getMessage(), error);

                    return Mono.empty();
                })
                .retry(3)
                .then();
    }

    /**
     * Clean up after a document's download by removing all the staging information belonging to that document.
     */
    public Mono<Void> cleanup(final Path documentFileLocation) {
        log.debug("Cleaning up staging for document {}.", documentFileLocation.getFileName());

        return Mono.just(documentFileLocation)
                .map(stageFileLocation -> documentFileLocation.toFile().delete())
                .then();
    }
}
