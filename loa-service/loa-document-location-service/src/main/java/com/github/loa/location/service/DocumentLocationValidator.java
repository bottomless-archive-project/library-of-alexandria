package com.github.loa.location.service;

import com.github.loa.document.service.domain.DocumentType;
import com.github.loa.location.domain.DocumentLocation;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * This service validates that a provided location contains a document.
 */
@Service
public class DocumentLocationValidator {

    /**
     * Returns true if the location could contain a valid document the the application could archive.
     *
     * @param documentLocation the document location to validate
     * @return true if the location could contain a document, false otherwise
     */
    public boolean validDocumentLocation(final DocumentLocation documentLocation) {
        //Using getPath() to be able to crawl urls like: /example/examplefile.pdf?queryparam=value
        final String documentLocationPath = documentLocation.getLocation().getPath();

        return Arrays.stream(DocumentType.values())
                .anyMatch(documentType -> documentLocationPath.endsWith("." + documentType.getFileExtension()));
    }
}
