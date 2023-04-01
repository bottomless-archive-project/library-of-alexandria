package com.github.bottomlessarchive.loa.location.domain;

import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DocumentLocation {

    private final String id;
    private final String location;
    private final DocumentType type;
    private final String sourceName;
}
