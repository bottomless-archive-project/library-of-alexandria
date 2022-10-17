package com.github.bottomlessarchive.loa.web.view.location.controller;

import com.github.bottomlessarchive.loa.location.service.factory.DocumentLocationEntityFactory;
import com.github.bottomlessarchive.loa.web.view.location.response.DocumentLocationDebugResponse;
import com.github.bottomlessarchive.loa.web.view.location.service.DocumentLocationDebugResponseFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
public class DocumentLocationDebugController {

    private final DocumentLocationEntityFactory documentLocationEntityFactory;
    private final DocumentLocationDebugResponseFactory documentLocationDebugResponseFactory;

    @GetMapping("/location/{locationId}/debug")
    public DocumentLocationDebugResponse getLocationById(@PathVariable final String locationId) {
        return documentLocationEntityFactory.getDocumentLocation(locationId)
                .map(documentLocationDebugResponseFactory::newDocumentLocationDebugResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Document location not found with id " + locationId + "!"));
    }
}
