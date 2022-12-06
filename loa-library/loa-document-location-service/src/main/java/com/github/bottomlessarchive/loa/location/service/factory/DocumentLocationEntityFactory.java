package com.github.bottomlessarchive.loa.location.service.factory;

import com.github.bottomlessarchive.loa.location.domain.DocumentLocationResultType;
import com.github.bottomlessarchive.loa.location.repository.DocumentLocationRepository;
import com.github.bottomlessarchive.loa.location.repository.domain.DocumentLocationDatabaseEntity;
import com.github.bottomlessarchive.loa.location.service.factory.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.location.service.factory.domain.DocumentLocationCreationContext;
import com.github.bottomlessarchive.loa.number.service.HexConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DocumentLocationEntityFactory {

    private final HexConverter hexConverter;
    private final DocumentLocationRepository documentLocationRepository;

    public Optional<DocumentLocation> getDocumentLocation(final String id) {
        return documentLocationRepository.getById(hexConverter.decode(id))
                .map(documentLocationDatabaseEntity -> DocumentLocation.builder()
                        .id(hexConverter.encode(documentLocationDatabaseEntity.getId()))
                        .url(documentLocationDatabaseEntity.getUrl())
                        .source(documentLocationDatabaseEntity.getSource())
                        .downloaderVersion(documentLocationDatabaseEntity.getDownloaderVersion())
                        .downloadResultCode(DocumentLocationResultType.valueOf(documentLocationDatabaseEntity.getDownloadResultCode()))
                        .build()
                );
    }

    public boolean isDocumentLocationExistsOrCreate(
            final DocumentLocationCreationContext documentLocationCreationContext) {
        final DocumentLocationDatabaseEntity documentLocationDatabaseEntity = new DocumentLocationDatabaseEntity();
        documentLocationDatabaseEntity.setId(hexConverter.decode(documentLocationCreationContext.getId()));
        documentLocationDatabaseEntity.setSource(documentLocationCreationContext.getSource());
        documentLocationDatabaseEntity.setUrl(documentLocationCreationContext.getUrl());
        documentLocationDatabaseEntity.setDownloaderVersion(documentLocationCreationContext.getDownloaderVersion());

        return documentLocationRepository.existsOrInsert(documentLocationDatabaseEntity);
    }
}
