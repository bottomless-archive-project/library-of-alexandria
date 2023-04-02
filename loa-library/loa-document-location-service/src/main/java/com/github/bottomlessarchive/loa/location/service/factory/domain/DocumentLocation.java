package com.github.bottomlessarchive.loa.location.service.factory.domain;

import com.github.bottomlessarchive.loa.location.domain.DocumentLocationResultType;
import lombok.Builder;

@Builder
public record DocumentLocation(

        String id,
        String url,
        String source,
        int downloaderVersion,
        DocumentLocationResultType downloadResultCode
) {
}
