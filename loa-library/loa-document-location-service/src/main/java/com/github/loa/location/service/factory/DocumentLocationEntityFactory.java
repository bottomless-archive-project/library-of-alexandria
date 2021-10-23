package com.github.loa.location.service.factory;

import com.github.loa.location.repository.DocumentLocationRepository;
import com.github.loa.location.repository.domain.DocumentLocationDatabaseEntity;
import com.github.loa.location.service.factory.domain.DocumentLocationCreationContext;
import com.github.loa.repository.service.HexConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class DocumentLocationEntityFactory {

    private final HexConverter hexConverter;
    private final DocumentLocationRepository documentLocationRepository;

    public Mono<Boolean> isDocumentLocationExistsOrCreate(
            final DocumentLocationCreationContext documentLocationCreationContext) {
        final DocumentLocationDatabaseEntity documentLocationDatabaseEntity = new DocumentLocationDatabaseEntity();
        documentLocationDatabaseEntity.setId(hexConverter.decode(documentLocationCreationContext.getId()));
        documentLocationDatabaseEntity.setSource(documentLocationCreationContext.getSource());
        documentLocationDatabaseEntity.setUrl(documentLocationCreationContext.getUrl());
        documentLocationDatabaseEntity.setDownloaderVersion(documentLocationCreationContext.getDownloaderVersion());

        return documentLocationRepository.existsOrInsert(documentLocationDatabaseEntity);
    }

    public Mono<Long> getDocumentLocationCount() {
        return documentLocationRepository.count();
    }
}
