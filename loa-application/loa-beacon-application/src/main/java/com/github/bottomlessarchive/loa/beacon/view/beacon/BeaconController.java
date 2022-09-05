package com.github.bottomlessarchive.loa.beacon.view.beacon;

import com.github.bottomlessarchive.loa.beacon.service.DocumentLocationProcessor;
import com.github.bottomlessarchive.loa.beacon.service.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.beacon.view.beacon.request.VisitDocumentLocationsRequest;
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
    public List<Object> visitDocumentLocations(@RequestBody final VisitDocumentLocationsRequest visitDocumentLocationsRequest) {
        final List<DocumentLocation> documentLocations = visitDocumentLocationsRequest.getLocations().stream()
                .map(location -> DocumentLocation.builder()
                        .id(location.getId())
                        .location(location.getLocation())
                        .build()
                )
                .toList();

        return documentLocationProcessor.processLocations(documentLocations);
    }
}
