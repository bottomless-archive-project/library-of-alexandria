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
     * The origin location was found (i.e. the server or the folder) but the document is no-longer exists on the location.
     */
    NOT_FOUND,

    /**
     * The origin location was not found (i.e. the server or the folder). The cause can be that the domain expired, there is no server
     * behind the domain or the server is not running, or the disk where the document should be present is detached.
     */
    ORIGIN_NOT_FOUND,

    /**
     * The request timed out while downloading the document.
     */
    TIMEOUT,

    /**
     * There were an error with the connection while downloading the document.
     */
    CONNECTION_ERROR,

    /**
     * The document location requires authentication or authorization. For this reason we were unable to download it.
     */
    FORBIDDEN,

    /**
     * There was an error on the server side. Can happen with fairly random things (i.e.: 400 errors).
     */
    SERVER_ERROR,

    /**
     * Got a valid response from the document location, but it was empty.
     */
    EMPTY_BODY,

    INVALID
}
