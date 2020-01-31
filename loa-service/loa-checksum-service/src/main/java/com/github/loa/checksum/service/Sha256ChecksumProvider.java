package com.github.loa.checksum.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "loa.checksum.type", havingValue = "sha256")
public class Sha256ChecksumProvider implements ChecksumProvider {

    @Override
    public Mono<String> checksum(final String documentId, final Path documentContents) {
        return Mono.fromSupplier(() -> calculateChecksum(documentId, documentContents));
    }

    @SneakyThrows
    private String calculateChecksum(final String documentId, final Path documentContents) {
        try (final InputStream inputStream = new FileInputStream(documentContents.toFile())) {
            return DigestUtils.sha256Hex(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load document contents for document " + documentId
                    + " while calculating the checksum.");
        }
    }
}
