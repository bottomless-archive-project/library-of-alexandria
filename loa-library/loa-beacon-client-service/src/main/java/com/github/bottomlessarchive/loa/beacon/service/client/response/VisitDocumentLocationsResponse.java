package com.github.bottomlessarchive.loa.beacon.service.client.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class VisitDocumentLocationsResponse {

    private final List<DocumentLocationResultPartialResponse> results;
}
