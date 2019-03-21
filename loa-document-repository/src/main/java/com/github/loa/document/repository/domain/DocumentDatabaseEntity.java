package com.github.loa.document.repository.domain;

import lombok.Data;

import java.time.Instant;

@Data
public class DocumentDatabaseEntity {

    private String id;
    private String url;
    private String crc;
    private String source;
    private long fileSize;
    private Instant downloadDate;
    private int downloaderVersion;
    private String status;
}
