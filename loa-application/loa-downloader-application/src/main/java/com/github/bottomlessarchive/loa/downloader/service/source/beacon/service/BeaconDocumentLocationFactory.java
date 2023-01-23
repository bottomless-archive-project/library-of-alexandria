package com.github.bottomlessarchive.loa.downloader.service.source.beacon.service;

import com.github.bottomlessarchive.loa.beacon.service.client.domain.BeaconDocumentLocation;
import com.github.bottomlessarchive.loa.location.domain.DocumentLocation;
import org.springframework.stereotype.Service;

@Service
public class BeaconDocumentLocationFactory {

    public BeaconDocumentLocation newBeaconDocumentLocation(final DocumentLocation location) {
        return BeaconDocumentLocation.builder()
                .id(location.getId())
                .type(location.getType())
                .location(location.getLocation())
                .sourceName(location.getSourceName())
                .build();
    }
}
