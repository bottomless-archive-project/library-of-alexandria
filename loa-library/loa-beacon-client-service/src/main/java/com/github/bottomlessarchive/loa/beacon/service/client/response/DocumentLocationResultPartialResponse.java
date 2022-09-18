package com.github.bottomlessarchive.loa.beacon.service.client.response;

import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import lombok.Builder;
import lombok.Getter;

import java.util.Optional;
import java.util.UUID;

@Getter
@Builder
public class DocumentLocationResultPartialResponse {

    private final String id;
    private final String documentId;
    private final String checksum;
    private final long size;
    private final String resultType;
    private final String sourceName;
    private final DocumentType type;
}
