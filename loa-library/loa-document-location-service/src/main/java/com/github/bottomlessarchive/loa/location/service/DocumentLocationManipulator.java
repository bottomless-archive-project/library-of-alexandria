package com.github.bottomlessarchive.loa.location.service;

import com.github.bottomlessarchive.loa.location.repository.DocumentLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentLocationManipulator {

    private final DocumentLocationRepository documentLocationRepository;

    public void updateDownloadStatus(final String documentLocationId, final int downloadResultCode) {
        documentLocationRepository.updateDownloadResultCode(documentLocationId, downloadResultCode);
    }
}
