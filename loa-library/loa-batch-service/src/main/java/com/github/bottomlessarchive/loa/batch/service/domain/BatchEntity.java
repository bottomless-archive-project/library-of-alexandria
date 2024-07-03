package com.github.bottomlessarchive.loa.batch.service.domain;

import lombok.Builder;
import org.bson.codecs.pojo.annotations.BsonId;

import java.util.List;
import java.util.UUID;

@Builder
public record BatchEntity(

        @BsonId
        UUID id,

        BatchStatus status,
        List<UUID> documents
) {
}
