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
                        .id(hexConverter.encode(documentLocationDatabaseEntity.id()))
                        .url(documentLocationDatabaseEntity.url())
                        .source(documentLocationDatabaseEntity.source())
                        .downloaderVersion(documentLocationDatabaseEntity.downloaderVersion())
                        .downloadResultCode(DocumentLocationResultType.valueOf(documentLocationDatabaseEntity.downloadResultCode()))
                        .build()
                );
    }

    public boolean isDocumentLocationExistsOrCreate(final DocumentLocationCreationContext documentLocationCreationContext) {
        final DocumentLocationDatabaseEntity documentLocationDatabaseEntity = DocumentLocationDatabaseEntity.builder()
                .id(hexConverter.decode(documentLocationCreationContext.getId()))
                .url(documentLocationCreationContext.getUrl())
                .source(documentLocationCreationContext.getSource())
                .downloaderVersion(documentLocationCreationContext.getDownloaderVersion())
                .downloadResultCode(DocumentLocationResultType.UNKNOWN.name())
                .build();

        return documentLocationRepository.existsOrInsert(documentLocationDatabaseEntity);
    }
}
