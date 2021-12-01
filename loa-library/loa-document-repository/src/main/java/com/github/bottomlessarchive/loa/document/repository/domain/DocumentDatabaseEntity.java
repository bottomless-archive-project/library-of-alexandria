package com.github.bottomlessarchive.loa.document.repository.domain;

import lombok.Getter;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonId;

import java.time.Instant;
import java.util.Collections;
import java.util.Set;
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
    private Set<byte[]> sourceLocations;
    private String compression;
    private byte[] checksum;
    private long fileSize;
    private Instant downloadDate;
    private int downloaderVersion;

    public Set<byte[]> getSourceLocations() {
        // Older documents doesn't have sourceLocations populated
        return sourceLocations == null ? Collections.emptySet() : sourceLocations;
    }
}
