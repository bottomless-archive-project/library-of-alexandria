package com.github.bottomlessarchive.loa.location.service.factory.domain;

import com.github.bottomlessarchive.loa.location.domain.DocumentLocationResultType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DocumentLocation {

    private final String id;
    private final String url;
    private final String source;
    private final int downloaderVersion;
    private final DocumentLocationResultType downloadResultCode;
}
