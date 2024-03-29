package com.github.bottomlessarchive.loa.downloader.service.document;

import com.github.bottomlessarchive.loa.location.service.factory.DocumentLocationEntityFactory;
import com.github.bottomlessarchive.loa.location.service.factory.domain.DocumentLocationCreationContext;
import com.github.bottomlessarchive.loa.downloader.service.DocumentLocationCreationContextFactory;
import com.github.bottomlessarchive.loa.location.domain.DocumentLocation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentLocationEvaluator {

    private final DocumentLocationEntityFactory documentLocationEntityFactory;
    private final DocumentLocationCreationContextFactory documentLocationCreationContextFactory;

    public boolean shouldProcessDocumentLocation(final DocumentLocation documentLocation) {
        final DocumentLocationCreationContext documentLocationCreationContext =
                documentLocationCreationContextFactory.newCreatingContext(documentLocation);

        return !documentLocationEntityFactory.isDocumentLocationExistsOrCreate(documentLocationCreationContext);
    }
}
