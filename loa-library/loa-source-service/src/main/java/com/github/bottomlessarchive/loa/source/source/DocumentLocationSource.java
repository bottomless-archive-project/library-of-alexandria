package com.github.bottomlessarchive.loa.source.source;

import com.github.bottomlessarchive.loa.location.domain.DocumentLocation;

import java.util.stream.Stream;

/**
 * This class is streaming new {@link DocumentLocation}s that should be checked for new documents.
 */
public interface DocumentLocationSource {

    /**
     * This method is streaming new document locations that should be checked for new documents.
     *
     * @return document locations that should be checked for new documents
     */
    Stream<DocumentLocation> streamLocations();
}
