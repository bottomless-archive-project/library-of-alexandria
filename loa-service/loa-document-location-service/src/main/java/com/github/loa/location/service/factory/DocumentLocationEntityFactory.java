package com.github.loa.location.service.factory;

import com.github.loa.location.repository.DocumentLocationRepository;
import com.github.loa.location.repository.domain.DocumentLocationDatabaseEntity;
import com.github.loa.location.service.factory.domain.DocumentLocationCreationContext;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class DocumentLocationEntityFactory {

    private final DocumentLocationRepository documentLocationRepository;

    public Mono<Boolean> isDocumentLocationExistsOrCreate(
            final DocumentLocationCreationContext documentLocationCreationContext) {
        final DocumentLocationDatabaseEntity documentLocationDatabaseEntity = new DocumentLocationDatabaseEntity();
        documentLocationDatabaseEntity.setId(decodeId(documentLocationCreationContext.getId()));
        documentLocationDatabaseEntity.setSource(documentLocationCreationContext.getSource());
        documentLocationDatabaseEntity.setUrl(documentLocationCreationContext.getUrl());
        documentLocationDatabaseEntity.setDownloaderVersion(documentLocationCreationContext.getDownloaderVersion());

        return documentLocationRepository.existsOrInsert(documentLocationDatabaseEntity);
    }

    //TODO: Move this to a helper!
    private byte[] decodeId(final String id) {
        try {
            return Hex.decodeHex(id);
        } catch (final DecoderException e) {
            throw new RuntimeException("Unable to decode id: " + id + "!", e);
        }
    }
}
