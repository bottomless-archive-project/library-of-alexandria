package com.github.bottomlessarchive.loa.beacon.view.beacon;

import com.github.bottomlessarchive.loa.beacon.service.DocumentLocationProcessor;
import com.github.bottomlessarchive.loa.beacon.service.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.beacon.service.domain.DocumentLocationResult;
import com.github.bottomlessarchive.loa.beacon.view.beacon.request.VisitDocumentLocationsRequest;
import com.github.bottomlessarchive.loa.beacon.view.beacon.response.DocumentLocationResultPartialResponse;
import com.github.bottomlessarchive.loa.beacon.view.beacon.response.VisitDocumentLocationsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/beacon")
@RequiredArgsConstructor
public class BeaconController {

    private final DocumentLocationProcessor documentLocationProcessor;

    @PostMapping("/visit-document-locations")
    public VisitDocumentLocationsResponse visitDocumentLocations(
            @RequestBody final VisitDocumentLocationsRequest visitDocumentLocationsRequest) {
        final List<DocumentLocation> documentLocations = visitDocumentLocationsRequest.getLocations().stream()
                .map(location -> DocumentLocation.builder()
                        .id(location.getId())
                        .location(location.getLocation())
                        .type(location.getType())
                        .build()
                )
                .toList();

        final List<DocumentLocationResult> results = documentLocationProcessor.processLocations(documentLocations);

        return VisitDocumentLocationsResponse.builder()
                .results(
                        results.stream()
                                .map(documentLocationResult ->
                                        DocumentLocationResultPartialResponse.builder()
                                                .id(documentLocationResult.id())
                                                .size(documentLocationResult.size())
                                                .checksum(documentLocationResult.checksum())
                                                .resultType(documentLocationResult.resultType())
                                                .build()
                                )
                                .toList()
                )
                .build();
    }
}
