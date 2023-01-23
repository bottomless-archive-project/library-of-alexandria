package com.github.bottomlessarchive.loa.location.repository.domain;

import lombok.Builder;
import org.bson.codecs.pojo.annotations.BsonId;

@Builder
public record DocumentLocationDatabaseEntity(

        @BsonId
        byte[] id,
        String url,
        String source,
        int downloaderVersion,
        String downloadResultCode
) {
}
