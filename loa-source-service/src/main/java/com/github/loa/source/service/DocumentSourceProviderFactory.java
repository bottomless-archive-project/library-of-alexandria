package com.github.loa.source.service;

import com.github.loa.source.service.util.UrlEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class DocumentSourceProviderFactory {

    private final DocumentSourceProvider documentSourceProvider;
    private final UrlEncoder urlEncoder;

    public Stream<URL> openSource() {
        return documentSourceProvider.stream()
                //TODO: Move the filtering logic (is a pdf etc) here with any kind of validation
                .map(urlEncoder::encode);
    }
}
