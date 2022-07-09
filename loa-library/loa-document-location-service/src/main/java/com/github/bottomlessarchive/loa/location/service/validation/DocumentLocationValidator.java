package com.github.bottomlessarchive.loa.location.service.validation;

import com.github.bottomlessarchive.loa.location.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.location.service.validation.extension.FileExtensionValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * This service validates that a provided location contains a document.
 */
@Service
@RequiredArgsConstructor
public class DocumentLocationValidator {

    private final FileExtensionValidator fileExtensionValidator;

    /**
     * Returns true if the location could contain a valid document the application could archive.
     *
     * @param documentLocation the document location to validate
     * @return true if the location could contain a document, false otherwise
     */
    public boolean validDocumentLocation(final DocumentLocation documentLocation) {
        if (!documentLocation.getLocation().isValid()) {
            return false;
        }

        //Using getPath() to be able to crawl urls like: /example/examplefile.pdf?queryparam=value
        final String documentLocationPath = documentLocation.getLocation().toUrl().orElseThrow().getPath();

        return fileExtensionValidator.isValidPathWithExtension(documentLocationPath);
    }
}
