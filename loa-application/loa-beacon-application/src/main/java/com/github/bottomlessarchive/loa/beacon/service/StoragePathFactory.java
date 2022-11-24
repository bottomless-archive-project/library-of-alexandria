package com.github.bottomlessarchive.loa.beacon.service;

import com.github.bottomlessarchive.loa.beacon.configuration.BeaconConfigurationProperties;
import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StoragePathFactory {

    private final BeaconConfigurationProperties beaconConfigurationProperties;

    public Path buildStoragePath(final UUID documentId) {
        return Path.of(beaconConfigurationProperties.storagePath())
                .resolve(documentId.toString());
    }
}
