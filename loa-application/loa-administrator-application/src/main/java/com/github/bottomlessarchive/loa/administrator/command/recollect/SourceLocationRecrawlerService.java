package com.github.bottomlessarchive.loa.administrator.command.recollect;

import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.location.service.factory.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.stage.service.StageLocationFactory;
import com.github.bottomlessarchive.loa.stage.service.domain.StageLocation;
import com.github.bottomlessarchive.loa.url.service.downloader.FileDownloadManager;
import com.github.bottomlessarchive.loa.validator.service.DocumentFileValidator;
import com.github.bottomlessarchive.loa.vault.client.service.VaultClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
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

    public void recrawlSourceLocation(final DocumentLocation documentLocation, final DocumentEntity documentEntity) {
        final String documentRecrawlId = UUID.randomUUID().toString();

        if (log.isInfoEnabled()) {
            log.info("Downloading the recrawl target for document: {} with new staging id: {}.", documentEntity.getId(), documentRecrawlId);
        }

        final StageLocation stageLocation = stageLocationFactory.getLocation(documentRecrawlId, documentEntity.getType());

        fileDownloadManager.downloadFile(documentLocation.getId(), convertToURL(documentLocation), stageLocation.getPath());

        final boolean isValidDocument = documentFileValidator.isValidDocument(documentRecrawlId, documentEntity.getType());

        if (isValidDocument) {
            try (InputStream content = stageLocation.openStream()) {
                vaultClientService.replaceCorruptDocument(documentEntity, content.readAllBytes());
            } catch (IOException e) {
                throw new IllegalStateException("Failed to replace document!", e);
            }
        }

        stageLocation.cleanup();
    }

    private URL convertToURL(final DocumentLocation sourceLocation) {
        try {
            return new URL(sourceLocation.getUrl());
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Illegal URL: " + sourceLocation + "!", e);
        }
    }
}
