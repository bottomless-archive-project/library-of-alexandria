package com.github.bottomlessarchive.loa.downloader.service.document;

import com.github.bottomlessarchive.loa.location.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.type.DocumentTypeCalculator;
import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;

import java.net.URL;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentLocationProcessor {

    private final DocumentTypeCalculator documentTypeCalculator;
    private final DocumentLocationProcessingExecutor documentLocationProcessingExecutor;

    public void processDocumentLocation(final DocumentLocation documentLocation) {
        documentLocation.getLocation().toUrl()
                .flatMap(location -> documentTypeCalculator.calculate(location)
                        .map(value -> ImmutablePair.of(location, value))
                )
                .ifPresentOrElse(pair -> doProcessing(documentLocation, pair), () -> logInvalidType(documentLocation));
    }

    private void doProcessing(final DocumentLocation documentLocation, final ImmutablePair<URL, DocumentType> pair) {
        documentLocationProcessingExecutor.executeProcessing(documentLocation.getId(), documentLocation.getSourceName(),
                pair.getLeft(), pair.getRight());
    }

    private void logInvalidType(final DocumentLocation documentLocation) {
        log.debug("Document on location {} has an unknown document type!", documentLocation);
    }
}
