package com.github.bottomlessarchive.loa.location.service.factory.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DocumentLocationCreationContext {

    private final String id;
    private final String url;
    private final String source;
    private final int downloaderVersion;
}
