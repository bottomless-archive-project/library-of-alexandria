package com.github.bottomlessarchive.loa.document.repository.domain;

import org.bson.codecs.record.annotations.BsonId;

import java.time.Instant;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public record DocumentDatabaseEntity(

        @BsonId
        UUID id,

        String vault,
        String type,
        String status,
        String compression,

        String source,
        Set<byte[]> sourceLocations,

        byte[] checksum,
        long fileSize,

        int downloaderVersion,
        Instant downloadDate
) {


    public Set<byte[]> sourceLocations() {
        // Older documents doesn't have sourceLocations populated
        return sourceLocations == null ? Collections.emptySet() : sourceLocations;
    }
}
