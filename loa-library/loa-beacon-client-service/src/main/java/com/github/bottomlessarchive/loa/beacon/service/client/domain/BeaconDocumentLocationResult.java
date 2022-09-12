package com.github.bottomlessarchive.loa.beacon.service.client.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BeaconDocumentLocationResult {

    private final String id;
    private final String checksum;
    private final long size;
    private final String resultType;
}
