package com.github.loa.source.service;

import com.github.loa.document.service.domain.DocumentType;
import com.github.loa.url.service.UrlEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class DocumentSourceProviderFactory {

    private final DocumentSourceProvider documentSourceProvider;
    private final UrlEncoder urlEncoder;

    public Stream<URL> openSource() {
        return documentSourceProvider.stream()
                .filter(this::shouldDownload)
                .map(urlEncoder::encode)
                .flatMap(Optional::stream);
    }

    private boolean shouldDownload(final URL documentLocation) {
        //Using getPath() to be able to crawl urls like: /example/examplefile.pdf?queryparam=value
        final String path = documentLocation.getPath();

        return Arrays.stream(DocumentType.values()).anyMatch(
                documentType -> path.endsWith("." + documentType.getFileExtension()));
    }
}
