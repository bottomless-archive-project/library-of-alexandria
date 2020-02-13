package com.github.loa.document.repository.domain;

import lombok.Getter;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonId;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class DocumentDatabaseEntity {

    @BsonId
    private UUID id;
    private String type;
    private String status;
    private String source;
    private String compression;
    private String checksum;
    private long fileSize;
    private Instant downloadDate;
    private int downloaderVersion;
}
