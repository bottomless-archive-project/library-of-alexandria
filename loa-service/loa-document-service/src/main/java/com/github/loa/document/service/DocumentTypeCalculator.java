package com.github.loa.document.service;

import com.github.loa.document.service.domain.DocumentType;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.Arrays;
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
        return Arrays.stream(DocumentType.values())
                .filter(type -> documentLocation.getPath().endsWith("." + type.getFileExtension()))
                .findFirst();
    }
}
