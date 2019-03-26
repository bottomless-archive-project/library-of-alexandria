package com.github.loa.document.service.id.factory;

import java.net.URL;

/**
 * This factory creates semi-unique ids for documents.
 */
public interface DocumentIdFactory {

    /**
     * Create an semi-unique document id based on the document's location.
     *
     * @param documentLocation the location of the document to create the id for
     * @return the id for the document
     */
    String newDocumentId(final URL documentLocation);
}
