package com.github.bottomlessarchive.loa.web.view.document.service;

import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.indexer.service.search.DocumentSearchClient;
import com.github.bottomlessarchive.loa.location.service.factory.DocumentLocationEntityFactory;
import com.github.bottomlessarchive.loa.location.service.factory.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.vault.client.service.VaultClientService;
import com.github.bottomlessarchive.loa.web.view.document.response.DebugDocumentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DebugResponseFactory {

    private final VaultClientService vaultClientService;
    private final DocumentSearchClient documentSearchClient;
    private final DocumentLocationEntityFactory documentLocationEntityFactory;

    public DebugDocumentResponse newDebugDocumentResponse(final DocumentEntity documentEntity) {
        final boolean isInIndex = documentSearchClient.isDocumentInIndex(documentEntity.getId());
        final boolean isInVault = vaultClientService.documentExists(documentEntity);

        final List<String> documentLocations = documentEntity.getSourceLocations().stream()
                .flatMap(id -> documentLocationEntityFactory.getDocumentLocation(id).stream())
                .map(DocumentLocation::getUrl)
                .toList();

        return DebugDocumentResponse.builder()
                .id(documentEntity.getId())
                .isInIndex(isInIndex)
                .isInVault(isInVault)
                .vault(documentEntity.getVault())
                .type(documentEntity.getType())
                .status(documentEntity.getStatus())
                .compression(documentEntity.getCompression())
                .checksum(documentEntity.getChecksum())
                .fileSize(documentEntity.getFileSize())
                .downloadDate(documentEntity.getDownloadDate())
                .downloaderVersion(documentEntity.getDownloaderVersion())
                .sourceLocations(Set.copyOf(documentLocations))
                .build();
    }
}
