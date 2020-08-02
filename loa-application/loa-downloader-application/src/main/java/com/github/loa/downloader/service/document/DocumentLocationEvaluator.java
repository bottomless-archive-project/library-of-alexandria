package com.github.loa.downloader.service.document;

import com.github.loa.downloader.service.DocumentLocationCreationContextFactory;
import com.github.loa.location.domain.DocumentLocation;
import com.github.loa.location.service.factory.DocumentLocationEntityFactory;
import com.github.loa.location.service.factory.domain.DocumentLocationCreationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class DocumentLocationEvaluator {

    private final DocumentLocationEntityFactory documentLocationEntityFactory;
    private final DocumentLocationCreationContextFactory documentLocationCreationContextFactory;

    public Mono<DocumentLocation> evaluateDocumentLocation(final DocumentLocation documentLocation) {
        final DocumentLocationCreationContext documentLocationCreationContext =
                documentLocationCreationContextFactory.newCreatingContext(documentLocation);

        return documentLocationEntityFactory.isDocumentLocationExistsOrCreate(documentLocationCreationContext)
                .filter(exists -> !exists)
                .map(result -> documentLocation);
    }
}
