package com.github.bottomlessarchive.loa.batch.repository.domain;

import lombok.Builder;
import org.bson.codecs.pojo.annotations.BsonId;

import java.util.List;
import java.util.UUID;

@Builder
public record BatchDatabaseEntity(

        @BsonId
        UUID id,

        String status,
        List<UUID> documents
) {
}
