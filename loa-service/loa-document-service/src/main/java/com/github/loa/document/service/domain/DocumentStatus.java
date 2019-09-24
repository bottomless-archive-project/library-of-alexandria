package com.github.loa.document.service.domain;

public enum DocumentStatus {

    /**
     * The document was successfully downloaded and moved to the vault.
     */
    DOWNLOADED,
    /**
     * The document is indexed to the search engine.
     */
    INDEXED,
    /**
     * An attempt was made to index the document but it failed.
     */
    INDEXING_FAILURE,
    /**
     * The document's content is removed from the vault. The reason for this could vary but most likely the document
     * was either password protected or corrupt.
     */
    REMOVED
}
