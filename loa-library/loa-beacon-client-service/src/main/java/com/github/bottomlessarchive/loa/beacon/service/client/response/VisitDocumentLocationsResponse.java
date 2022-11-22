package com.github.bottomlessarchive.loa.beacon.service.client.response;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Getter
@Builder
@Jacksonized
public class VisitDocumentLocationsResponse {

    private final List<DocumentLocationResultPartialResponse> results;
}
