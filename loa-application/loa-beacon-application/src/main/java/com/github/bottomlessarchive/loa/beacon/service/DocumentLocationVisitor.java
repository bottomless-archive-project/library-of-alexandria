package com.github.bottomlessarchive.loa.beacon.service;

import com.github.bottomlessarchive.loa.beacon.service.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.stage.service.StageLocationFactory;
import com.github.bottomlessarchive.loa.stage.service.domain.StageLocation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URL;

@Service
@RequiredArgsConstructor
public class DocumentLocationVisitor {

    private final StageLocationFactory stageLocationFactory;

    public Object visitDocumentLocation(final DocumentLocation documentLocation) {
        try {
            final URL documentLocationURL = new URL(documentLocation.getLocation());

            final StageLocation stageLocation = stageLocationFactory.getLocation(documentLocation.getId(), documentLocation.getType());

            return null; //TODO
        } catch (Exception e) {
            return null;
        }
    }
}
