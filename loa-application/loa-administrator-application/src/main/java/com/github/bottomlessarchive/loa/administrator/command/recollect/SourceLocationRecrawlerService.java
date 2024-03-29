package com.github.bottomlessarchive.loa.administrator.command.recollect;

import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.io.service.downloader.FileDownloadManager;
import com.github.bottomlessarchive.loa.location.service.factory.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.stage.service.StageLocationFactory;
import com.github.bottomlessarchive.loa.stage.service.domain.StageLocation;
import com.github.bottomlessarchive.loa.validator.service.DocumentFileValidator;
import com.github.bottomlessarchive.loa.vault.client.service.VaultClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "loa.command", havingValue = "recollect-corrupt-documents")
public class SourceLocationRecrawlerService {

    private final StageLocationFactory stageLocationFactory;
    private final DocumentFileValidator documentFileValidator;
    private final VaultClientService vaultClientService;
    private final FileDownloadManager fileDownloadManager;

    public void recrawlSourceLocation(final DocumentLocation documentLocation, final DocumentEntity documentEntity) {
        final UUID documentRecrawlId = UUID.randomUUID();

        if (log.isInfoEnabled()) {
            log.info("Downloading the recrawl target for document: {} with new staging id: {}.", documentEntity.getId(), documentRecrawlId);
        }

        try (StageLocation stageLocation = stageLocationFactory.getLocation(documentRecrawlId)) {
            fileDownloadManager.downloadFile(documentLocation.url(), stageLocation.getPath());

            final boolean isValidDocument = documentFileValidator.isValidDocument(
                    documentRecrawlId, stageLocation, documentEntity.getType());

            if (isValidDocument) {
                try (InputStream content = stageLocation.openStream()) {
                    vaultClientService.replaceCorruptDocument(documentEntity, content.readAllBytes());
                } catch (IOException e) {
                    throw new IllegalStateException("Failed to replace document!", e);
                }
            }
        }
    }
}
