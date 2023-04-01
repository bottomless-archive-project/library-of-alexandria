package com.github.bottomlessarchive.loa.loadoc.domain;

import lombok.Builder;

@Builder
public record LoadocMetadata(

        String id,
        String type,
        String sourceName,
        String documentLocationId,
        String downloadResultCode
) {
}
