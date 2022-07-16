package com.github.bottomlessarchive.loa.document.service;

import com.github.bottomlessarchive.loa.document.service.domain.DocumentType;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

@Service
public class DocumentTypeCalculator {

    /**
     * Calculate the document's type from the provided document location.
     *
     * @param documentLocation the location to calculate the type for
     * @return the calculated type
     */
    public Optional<DocumentType> calculate(final URL documentLocation) {
        final String path = documentLocation.getPath().toLowerCase(Locale.ENGLISH);

        if (path.endsWith(".fb2.zip")) {
            return Optional.of(DocumentType.FB2);
        }

        return Arrays.stream(DocumentType.values())
                .filter(type -> path.endsWith("." + type.getFileExtension()))
                .findFirst();
    }
}
