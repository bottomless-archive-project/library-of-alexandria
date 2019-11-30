package com.github.loa.checksum.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "loa.checksum.type", havingValue = "sha256")
public class Sha256ChecksumProvider implements ChecksumProvider {

    @Override
    public Mono<String> checksum(final String documentId, final Path documentContents) {
        return Mono.fromSupplier(() -> {
            try {
                return DigestUtils.sha256Hex(Files.newInputStream(documentContents));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
