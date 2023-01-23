package com.github.bottomlessarchive.loa.document.repository.domain;

import lombok.Builder;
import org.bson.codecs.pojo.annotations.BsonId;

import java.time.Instant;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

@Builder
public record DocumentDatabaseEntity(

        @BsonId
        UUID id,

        String vault,
        String type,
        String status,
        String compression,

        String source,
        String beacon,
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
