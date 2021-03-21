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
     *
     * @deprecated Use {@link #CORRUPT} instead.
     */
    @Deprecated
    INDEXING_FAILURE,

    /**
     * The document is not available in the vault, or the data that is available are corrupt in any for or shape. This means that a corrupt
     * document mostly likely can't be opened, indexed or restored.
     */
    CORRUPT
}
