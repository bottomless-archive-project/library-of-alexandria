package com.github.loa.location.service.id.factory;

import java.net.URL;

/**
 * This factory creates semi-unique ids for document locations.
 */
public interface DocumentLocationIdFactory {

    /**
     * Create an semi-unique document location id based on the url.
     *
     * @param documentLocation the location of the document to create the id for
     * @return the id for the document
     */
    String newDocumentId(final URL documentLocation);
}
