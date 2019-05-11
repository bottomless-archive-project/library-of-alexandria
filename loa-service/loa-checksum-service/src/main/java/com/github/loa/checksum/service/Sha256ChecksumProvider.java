package com.github.loa.checksum.service;

import com.github.loa.document.service.domain.DocumentType;
import com.github.loa.stage.service.StageLocationFactory;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "loa.checksum.type", havingValue = "sha256")
public class Sha256ChecksumProvider implements ChecksumProvider {

    private final StageLocationFactory stageLocationFactory;

    @Override
    public String checksum(final String documentId, final DocumentType documentType) {
        final File stageFileLocation = stageLocationFactory.getLocation(documentId, documentType);

        try {
            try (final BufferedInputStream documentInputStream =
                         new BufferedInputStream(new FileInputStream(stageFileLocation))) {
                return DigestUtils.sha256Hex(documentInputStream);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to calculate file hash!", e);
        }
    }
}
