package com.github.bottomlessarchive.loa.beacon.service.domain;

import com.github.bottomlessarchive.loa.location.domain.DocumentLocationResultType;
import lombok.Builder;

@Builder
public record DocumentLocationResult(

        String id,
        String checksum,
        long size,
        DocumentLocationResultType resultType
) {
}
