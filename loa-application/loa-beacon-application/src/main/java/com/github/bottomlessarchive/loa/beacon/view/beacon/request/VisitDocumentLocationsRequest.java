package com.github.bottomlessarchive.loa.beacon.view.beacon.request;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Getter
@Builder
@Jacksonized
public class VisitDocumentLocationsRequest {

    private final List<DocumentLocationPartialRequest> locations;
}
