package com.github.bottomlessarchive.loa.beacon.service.client.domain;

import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BeaconDocumentLocation {

    private final String id;
    private final String location;
    private final DocumentType type;
}
