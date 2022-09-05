package com.github.bottomlessarchive.loa.beacon.service;

import com.github.bottomlessarchive.loa.beacon.service.domain.DocumentLocation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentLocationProcessor {

    private final DocumentLocationVisitor documentLocationVisitor;

    public List<Object> processLocations(final List<DocumentLocation> documentLocations) {
        return documentLocations.stream()
                .map(documentLocationVisitor::visitDocumentLocation)
                .toList();
    }
}
