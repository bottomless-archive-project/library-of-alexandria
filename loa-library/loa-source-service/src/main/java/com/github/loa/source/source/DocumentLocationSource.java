package com.github.loa.source.source;

import com.github.loa.location.domain.DocumentLocation;
import reactor.core.publisher.Flux;

/**
 * This class is streaming new URLs that should be checked for new documents.
 */
public interface DocumentLocationSource {

    /**
     * This method is streaming new document locations that should be checked for new documents.
     *
     * @return document locations that should be checked for new documents
     */
    Flux<DocumentLocation> streamLocations();
}
