package com.github.bottomlessarchive.loa.administrator.command.recollect;

import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentType;
import com.github.bottomlessarchive.loa.stage.service.StageLocationFactory;
import com.github.bottomlessarchive.loa.stage.service.domain.StageLocation;
import com.github.bottomlessarchive.loa.url.service.downloader.FileDownloadManager;
import com.github.bottomlessarchive.loa.validator.service.DocumentFileValidator;
import com.github.bottomlessarchive.loa.vault.client.service.VaultClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty("recollect-corrupt-documents")
public class SourceLocationRecrawlerService {

    private final StageLocationFactory stageLocationFactory;
    private final DocumentFileValidator documentFileValidator;
    private final VaultClientService vaultClientService;
    private final FileDownloadManager fileDownloadManager;

    public Mono<DocumentEntity> recrawlSourceLocation(final URL sourceLocation, final DocumentEntity documentEntity) {
        final String documentRecrawlId = UUID.randomUUID().toString();

        return stageLocationFactory.getLocation(documentRecrawlId, documentEntity.getType())
                .flatMap(stageFileLocation -> acquireFile(sourceLocation, stageFileLocation))
                .flatMap(documentFileLocation -> documentFileValidator.isValidDocument(documentRecrawlId, documentEntity.getType())
                        .filter(validationResult -> !validationResult)
                        .flatMap(validationResult -> documentFileLocation.cleanup())
                        .thenReturn(documentFileLocation)
                )
                .filterWhen(StageLocation::exists)
                .map(location -> {
                    try (final InputStream content = location.openStream()) {
                        return vaultClientService.replaceCorruptDocument(documentEntity, content.readAllBytes());
                    } catch (IOException e) {
                        throw new RuntimeException();
                    }
                })
                .flatMap(location -> cleanup(documentRecrawlId, documentEntity.getType()))
                .onErrorResume(error -> {
                    if (log.isDebugEnabled()) {
                        log.debug("Error downloading a document: {}!", error.getMessage());
                    }

                    return Mono.empty();
                })
                .then(Mono.just(documentEntity));
    }

    private Mono<StageLocation> acquireFile(final URL documentLocation, final StageLocation stageLocation) {
        return fileDownloadManager.downloadFile(documentLocation, stageLocation.getPath())
                .thenReturn(stageLocation);
    }

    private Mono<Void> cleanup(final String documentRecrawlId, final DocumentType documentType) {
        return stageLocationFactory.getLocation(documentRecrawlId, documentType)
                .flatMap(StageLocation::cleanup);
    }
}
