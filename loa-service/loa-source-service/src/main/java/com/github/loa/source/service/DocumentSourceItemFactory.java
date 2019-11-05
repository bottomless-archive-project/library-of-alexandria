package com.github.loa.source.service;

import com.github.loa.source.configuration.DocumentSourceConfiguration;
import com.github.loa.source.domain.DocumentSourceItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URL;

@Service
@RequiredArgsConstructor
public class DocumentSourceItemFactory {

    private final DocumentSourceConfiguration documentSourceConfiguration;

    public DocumentSourceItem newDocumentSourceItem(final URL documentLocation) {
        return DocumentSourceItem.builder()
                .sourceName(documentSourceConfiguration.getName())
                .documentLocation(documentLocation)
                .build();
    }
}
