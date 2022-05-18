package com.github.bottomlessarchive.loa.location.repository.domain;

import org.bson.codecs.pojo.annotations.BsonId;

public record DocumentLocationDatabaseEntity(

        @BsonId
        byte[] id,
        String url,
        String source,
        int downloaderVersion
) {
}
