package com.github.bottomlessarchive.loa.location.repository.domain;

import org.bson.codecs.record.annotations.BsonId;

public record DocumentLocationDatabaseEntity(

        @BsonId
        byte[] id,
        String url,
        String source,
        int downloaderVersion,
        String downloadResultCode
) {
}
