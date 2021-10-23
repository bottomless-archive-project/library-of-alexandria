package com.github.loa.queue.service.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Queue {

    /**
     * This queue is responsible to hold the document locations created by the Generator Application.
     */
    DOCUMENT_LOCATION_QUEUE("loa-document-location", "loa-document-location"),
    /**
     * This queue is responsible to hold the documents downloaded by the Downloader Application.
     */
    DOCUMENT_ARCHIVING_QUEUE("loa-document-archiving", "loa-document-archiving");

    private final String name;
    private final String address;
}
