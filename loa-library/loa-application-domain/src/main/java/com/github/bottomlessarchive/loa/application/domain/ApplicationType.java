package com.github.bottomlessarchive.loa.application.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApplicationType {

    ADMINISTRATOR_APPLICATION(false),
    CONDUCTOR_APPLICATION(false),
    DOWNLOADER_APPLICATION(true),
    GENERATOR_APPLICATION(true),
    INDEXER_APPLICATION(true),
    QUEUE_APPLICATION(true),
    VAULT_APPLICATION(true),
    WEB_APPLICATION(true),
    STAGING_APPLICATION(true),
    DOCUMENT_DATABASE(true),
    DOCUMENT_INDEX(true);

    private final boolean reportStatusAndLocation;
}
