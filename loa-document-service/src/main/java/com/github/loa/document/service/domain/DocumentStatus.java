package com.github.loa.document.service.domain;

public enum DocumentStatus {

    /**
     * The download was failed.
     */
    FAILED,
    /**
     * Used when a document is a duplicate of an other document already crawled.
     */
    DUPLICATE,
    /**
     * Used when the file downloaded was not a valid pdf.
     */
    INVALID,
    /**
     * The document was successfuly downloaded and moved to the target area.
     */
    DOWNLOADED
}
