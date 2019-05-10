package com.github.loa.document.service.location.factory;

import com.github.loa.document.repository.DocumentLocationRepository;
import com.github.loa.document.repository.domain.DocumentLocationDatabaseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URL;

@Service
@RequiredArgsConstructor
public class DocumentLocationEntityFactory {

    private final DocumentLocationRepository documentLocationRepository;

    public boolean isDocumentLocationExists(final String documentLocationId) {
        return documentLocationRepository.findById(documentLocationId) != null;
    }

    public void newDocumentLocationEntity(final String id, final URL location, final int versionNumber,
            final String source) {
        final DocumentLocationDatabaseEntity documentLocationDatabaseEntity = new DocumentLocationDatabaseEntity();

        documentLocationDatabaseEntity.setId(id);
        documentLocationDatabaseEntity.setUrl(location.toString());
        documentLocationDatabaseEntity.setDownloaderVersion(versionNumber);
        documentLocationDatabaseEntity.setSource(source);

        documentLocationRepository.insertDocumentLocation(documentLocationDatabaseEntity);
    }
}
