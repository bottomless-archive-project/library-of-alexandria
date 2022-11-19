package com.github.bottomlessarchive.loa.downloader.service.document;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DocumentIdFactory {

    public UUID newDocumentId() {
        return UUID.randomUUID();
    }
}
