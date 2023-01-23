package com.github.bottomlessarchive.loa.document.service.domain;

public enum DocumentStatus {

    /**
     * The document was created in the database but not yet fully moved into the vault.
     */
    CREATED,

    /**
     * The document was successfully downloaded and moved to the vault.
     */
    DOWNLOADED,

    /**
     * The document is indexed to the search engine.
     */
    INDEXED,

    /**
     * The document is not in the vaults but on one of the beacon machines.
     */
    ON_BEACON,

    /**
     * The document is not available in the vault, or the data that is available are corrupt in any for or shape. This means that a corrupt
     * document mostly likely can't be opened, indexed or restored.
     */
    CORRUPT
}
