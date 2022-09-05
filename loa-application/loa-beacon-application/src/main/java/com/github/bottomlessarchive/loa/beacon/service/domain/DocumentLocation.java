package com.github.bottomlessarchive.loa.beacon.service.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DocumentLocation {

    private final String id;
    private final String location;
}
