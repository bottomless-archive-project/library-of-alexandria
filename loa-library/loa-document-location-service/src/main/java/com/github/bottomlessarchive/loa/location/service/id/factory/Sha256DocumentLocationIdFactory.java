package com.github.bottomlessarchive.loa.location.service.id.factory;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import java.net.URL;

/**
 * An SHA-256 based implementation for the {@link DocumentLocationIdFactory}.
 */
@Service
public class Sha256DocumentLocationIdFactory implements DocumentLocationIdFactory {

    /**
     * Create a semi-unique document id based on the SHA-256 hash of the document's location.
     *
     * @param documentLocation the location of the document to create the id for
     * @return the id for the document
     */
    @Override
    public String newDocumentLocationId(final URL documentLocation) {
        return DigestUtils.sha256Hex(documentLocation.toString());
    }
}
