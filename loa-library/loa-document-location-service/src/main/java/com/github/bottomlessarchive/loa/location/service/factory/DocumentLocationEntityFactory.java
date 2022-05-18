package com.github.bottomlessarchive.loa.location.service.factory;

import com.github.bottomlessarchive.loa.location.repository.DocumentLocationRepository;
import com.github.bottomlessarchive.loa.location.repository.domain.DocumentLocationDatabaseEntity;
import com.github.bottomlessarchive.loa.location.service.factory.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.location.service.factory.domain.DocumentLocationCreationContext;
import com.github.bottomlessarchive.loa.repository.service.HexConverter;
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
                        .build()
                );
    }

    public boolean isDocumentLocationExistsOrCreate(
            final DocumentLocationCreationContext documentLocationCreationContext) {
        final DocumentLocationDatabaseEntity documentLocationDatabaseEntity = new DocumentLocationDatabaseEntity(
                hexConverter.decode(documentLocationCreationContext.getId()),
                documentLocationCreationContext.getUrl(),
                documentLocationCreationContext.getSource(),
                documentLocationCreationContext.getDownloaderVersion()
        );

        return documentLocationRepository.existsOrInsert(documentLocationDatabaseEntity);
    }
}
