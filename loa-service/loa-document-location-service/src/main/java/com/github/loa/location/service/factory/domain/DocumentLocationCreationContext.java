package com.github.loa.location.service.factory.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DocumentLocationCreationContext {

    private String id;
    private String url;
    private String source;
    private int downloaderVersion;
}
