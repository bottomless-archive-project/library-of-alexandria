package com.github.bottomlessarchive.loa.application.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApplicationType {

    ADMINISTRATOR_APPLICATION,
    CONDUCTOR_APPLICATION,
    DOWNLOADER_APPLICATION,
    BEACON_APPLICATION,
    GENERATOR_APPLICATION,
    INDEXER_APPLICATION,
    QUEUE_APPLICATION,
    VAULT_APPLICATION,
    WEB_APPLICATION,
    STAGING_APPLICATION,
    DOCUMENT_DATABASE,
    DOCUMENT_INDEX
}
