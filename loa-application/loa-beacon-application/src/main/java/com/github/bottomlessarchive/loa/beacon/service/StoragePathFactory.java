package com.github.bottomlessarchive.loa.beacon.service;

import com.github.bottomlessarchive.loa.beacon.configuration.BeaconConfigurationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StoragePathFactory {

    private final BeaconConfigurationProperties beaconConfigurationProperties;

    public Path buildStoragePath(final UUID documentId) {
        return beaconConfigurationProperties.storageDirectory()
                .resolve(documentId.toString());
    }
}
