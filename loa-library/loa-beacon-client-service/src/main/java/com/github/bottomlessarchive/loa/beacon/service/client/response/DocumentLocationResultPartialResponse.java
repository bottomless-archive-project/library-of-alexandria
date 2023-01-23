package com.github.bottomlessarchive.loa.beacon.service.client.response;

import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class DocumentLocationResultPartialResponse {

    private final String id;
    private final String documentId;
    private final String checksum;
    private final long size;
    private final String resultType;
    private final String sourceName;
    private final DocumentType type;
}
