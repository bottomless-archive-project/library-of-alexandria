package com.github.bottomlessarchive.loa.beacon.view.beacon;

import com.github.bottomlessarchive.loa.beacon.service.DocumentLocationProcessor;
import com.github.bottomlessarchive.loa.beacon.service.StoragePathFactory;
import com.github.bottomlessarchive.loa.beacon.service.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.beacon.service.domain.DocumentLocationResult;
import com.github.bottomlessarchive.loa.beacon.view.beacon.request.VisitDocumentLocationsRequest;
import com.github.bottomlessarchive.loa.beacon.view.beacon.response.DocumentLocationResultPartialResponse;
import com.github.bottomlessarchive.loa.beacon.view.beacon.response.VisitDocumentLocationsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/beacon")
@RequiredArgsConstructor
public class BeaconController {

    private final StoragePathFactory storagePathFactory;
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

    @GetMapping("/document/{documentId}")
    public Resource returnDownloadedDocument(@PathVariable final UUID documentId) {
        return new FileSystemResource(storagePathFactory.buildStoragePath(documentId));
    }

    @DeleteMapping("/document/{documentId}")
    public void deleteDownloadedDocument(@PathVariable final UUID documentId) throws IOException {
        Files.delete(storagePathFactory.buildStoragePath(documentId));
    }
}
