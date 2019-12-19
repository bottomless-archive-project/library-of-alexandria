package com.github.loa.downloader.service.file;

import com.github.loa.vault.client.service.VaultClientService;
import com.github.loa.vault.client.service.domain.ArchivingContext;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * This service is responsible for the manipulating of document files. For example moving them to the vault or removing
 * them if necessary.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentFileManipulator {

    private final VaultClientService vaultClientService;
    @Qualifier("archivedDocumentCount")
    private final Counter archivedDocumentCount;

    /**
     * Move a document's file from the staging area to the vault. The document's metadata is not updated by this method!
     */
    public Mono<Void> moveToVault(final ArchivingContext archivingContext) {
        log.info("Moving document to vault {}!", archivingContext.getContents().getFileName());

        return Mono.just(archivingContext)
                .flatMap(this::archiveDocument)
                .doOnNext(this::incrementArchivedCount)
                .flatMap(this::cleanup)
                .onErrorResume(error -> {
                    log.error("Error archiving a document: {}!", error.getMessage(), error);

                    return cleanup(archivingContext);
                })
                .retry(3)
                .then();
    }

    private Mono<ArchivingContext> archiveDocument(final ArchivingContext archivingContext) {
        return vaultClientService.archiveDocument(archivingContext)
                .thenReturn(archivingContext);
    }

    private void incrementArchivedCount(final ArchivingContext archivingContext) {
        archivedDocumentCount.increment();
    }

    /**
     * Clean up after a document's download by removing all the staging information belonging to that document.
     */
    private Mono<Void> cleanup(final ArchivingContext archivingContext) {
        log.debug("Cleaning up staging for document {}.", archivingContext.getContents().getFileName());

        return Mono.just(archivingContext)
                .map(stageFileLocation -> archivingContext.getContents().toFile().delete())
                .then();
    }
}
