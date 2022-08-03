package com.github.bottomlessarchive.loa.location.domain;

public enum DocumentLocationResultType {

    /**
     * The document location responded with a document. It is not guaranteed that the document is valid tough.
     */
    OK,

    /**
     * The result is not known yet because the document is still downloading, or the result of the download is not (yet) supported/mapped.
     */
    UNKNOWN,

    /**
     * The document location is no-longer exists.
     */
    NOT_FOUND,

    /**
     * The document location requires authentication or authorization. For this reason we were unable to download it.
     */
    FORBIDDEN,

    /**
     * Got a valid response from the document location, but it was empty.
     */
    EMPTY_BODY
}
