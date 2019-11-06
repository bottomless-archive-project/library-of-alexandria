package com.github.loa.document.service.location.factory;

import com.github.loa.document.repository.DocumentLocationRepository;
import com.github.loa.document.repository.domain.DocumentLocationDatabaseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class DocumentLocationEntityFactory {

    private final DocumentLocationRepository documentLocationRepository;

    public Mono<Boolean> isDocumentLocationExistsOrCreate(
            final DocumentLocationDatabaseEntity documentLocationDatabaseEntity) {
        return documentLocationRepository.existsOrInsert(documentLocationDatabaseEntity);
    }
}
