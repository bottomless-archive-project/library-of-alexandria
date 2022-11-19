package com.github.bottomlessarchive.loa.beacon.service.client.request;

import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class DocumentLocationPartialRequest {

    private final String id;
    private final String location;
    private final DocumentType type;
    private final String sourceName;
}
