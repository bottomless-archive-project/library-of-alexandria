package com.github.loa.checksum.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "loa.checksum.type", havingValue = "sha256")
public class Sha256ChecksumProvider implements ChecksumProvider {

    @Override
    public Mono<String> checksum(final byte[] documentContents) {
        return Mono.fromSupplier(() -> calculateChecksum(documentContents));
    }

    @SneakyThrows
    private String calculateChecksum(final byte[] documentContents) {
        return DigestUtils.sha256Hex(documentContents);
    }
}
