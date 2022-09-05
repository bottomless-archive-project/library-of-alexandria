package com.github.bottomlessarchive.loa.beacon.view.beacon.request;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class DocumentLocationPartialRequest {

    private final String id;
    private final String location;
}
