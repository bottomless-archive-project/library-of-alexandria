package com.github.bottomlessarchive.loa.document.repository.domain;

import lombok.Getter;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonId;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class DocumentDatabaseEntity {

    @BsonId
    private UUID id;
    private String vault;
    private String type;
    private String status;
    private String source;
    private List<byte[]> sourceLocations;
    private String compression;
    private byte[] checksum;
    private long fileSize;
    private Instant downloadDate;
    private int downloaderVersion;
}
