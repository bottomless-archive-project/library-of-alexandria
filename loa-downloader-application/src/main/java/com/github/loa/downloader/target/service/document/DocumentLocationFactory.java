package com.github.loa.downloader.target.service.document;

import com.github.loa.downloader.target.configuration.DocumentTargetConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * A factory that creates final saving {@link java.io.File} location instances.
 */
@Service
@RequiredArgsConstructor
public class DocumentLocationFactory {

    private final DocumentTargetConfiguration documentTargetConfiguration;

    public File newLocation(final String documentId) {
        return new File(documentTargetConfiguration.getLocation(), documentId + ".pdf");
    }
}
