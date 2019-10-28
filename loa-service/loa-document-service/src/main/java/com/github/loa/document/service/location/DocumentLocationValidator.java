package com.github.loa.document.service.location;

import com.github.loa.document.service.domain.DocumentType;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.Arrays;

@Service
public class DocumentLocationValidator {

    public boolean validDocumentLocation(final URL documentLocation) {
        //Using getPath() to be able to crawl urls like: /example/examplefile.pdf?queryparam=value
        final String documentLocationPath = documentLocation.getPath();

        return Arrays.stream(DocumentType.values())
                .anyMatch(documentType -> documentLocationPath.endsWith("." + documentType.getFileExtension()));
    }
}
