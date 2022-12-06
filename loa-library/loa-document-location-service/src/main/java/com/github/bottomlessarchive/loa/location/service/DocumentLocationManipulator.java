package com.github.bottomlessarchive.loa.location.service;

import com.github.bottomlessarchive.loa.location.domain.DocumentLocationResultType;
import com.github.bottomlessarchive.loa.location.repository.DocumentLocationRepository;
import com.github.bottomlessarchive.loa.number.service.HexConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentLocationManipulator {

    private final HexConverter hexConverter;
    private final DocumentLocationRepository documentLocationRepository;

    public void updateDownloadResultCode(final String documentLocationId, final DocumentLocationResultType downloadResultCode) {
        documentLocationRepository.updateDownloadResultCode(hexConverter.decode(documentLocationId), downloadResultCode.toString());
    }
}
