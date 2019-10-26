package com.github.loa.downloader.download.service.file;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.domain.DocumentType;
import com.github.loa.stage.service.StageLocationFactory;
import com.github.loa.vault.client.service.VaultClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * This service is responsible for the manipulating of document files. For example moving them to the vault or removing
 * them if necessary.
 */
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
        return stageLocationFactory.getLocation(documentEntity)
                .flatMap(documentLocation -> {
                    try (final InputStream documentContents = new FileInputStream(documentLocation)) {
                        //TODO: Archive should be reactive!
                        vaultClientService.archiveDocument(documentEntity, documentContents);
                    } catch (Exception e) {
                        return Mono.empty();
                    }

                    return Mono.just(documentLocation);
                })
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
        return stageLocationFactory.getLocation(documentId, documentType)
                .map(File::delete)
                .then();
    }
}
