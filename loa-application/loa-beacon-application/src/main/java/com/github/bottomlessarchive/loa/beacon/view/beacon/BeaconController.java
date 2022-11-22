package com.github.bottomlessarchive.loa.beacon.view.beacon;

import com.github.bottomlessarchive.loa.beacon.service.DocumentLocationProcessor;
import com.github.bottomlessarchive.loa.beacon.service.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.beacon.service.domain.DocumentLocationResult;
import com.github.bottomlessarchive.loa.beacon.view.beacon.request.VisitDocumentLocationsRequest;
import com.github.bottomlessarchive.loa.beacon.view.beacon.response.DocumentLocationResultPartialResponse;
import com.github.bottomlessarchive.loa.beacon.view.beacon.response.VisitDocumentLocationsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/beacon")
@RequiredArgsConstructor
public class BeaconController {

    private final DocumentLocationProcessor documentLocationProcessor;

    @PostMapping("/visit-document-locations")
    public VisitDocumentLocationsResponse visitDocumentLocations(
            @RequestBody final VisitDocumentLocationsRequest visitDocumentLocationsRequest) {
        log.info("Got {} document locations to visit.", visitDocumentLocationsRequest.getLocations().size());

        final List<DocumentLocation> documentLocations = visitDocumentLocationsRequest.getLocations().stream()
                .map(location -> DocumentLocation.builder()
                        .id(location.getId())
                        .location(location.getLocation())
                        .type(location.getType())
                        .sourceName(location.getSourceName())
                        .build()
                )
                .toList();

        final List<DocumentLocationResult> results = documentLocationProcessor.processLocations(documentLocations);

        log.info("Returning {} results for a crawl request.", results.size());

        return VisitDocumentLocationsResponse.builder()
                .results(
                        results.stream()
                                .map(documentLocationResult ->
                                        DocumentLocationResultPartialResponse.builder()
                                                .id(documentLocationResult.id())
                                                .documentId(documentLocationResult.documentId() != null
                                                        ? documentLocationResult.documentId().toString() : null)
                                                .size(documentLocationResult.size())
                                                .checksum(documentLocationResult.checksum())
                                                .resultType(documentLocationResult.resultType())
                                                .sourceName(documentLocationResult.sourceName())
                                                .type(documentLocationResult.type())
                                                .build()
                                )
                                .toList()
                )
                .build();
    }

    @PostMapping("/document/{documentId}")
    public byte[] returnDownloadedDocument(@PathVariable final UUID documentId) {
        return new byte[]{}; //TODO
    }
}