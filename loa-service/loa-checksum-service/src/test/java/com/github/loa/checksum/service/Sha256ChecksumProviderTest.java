package com.github.loa.checksum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URISyntaxException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Sha256ChecksumProviderTest {

    private static final String DOCUMENT_ID = "123456";

    private Sha256ChecksumProvider sha256ChecksumProvider;

    @BeforeEach
    private void setup() {
        sha256ChecksumProvider = new Sha256ChecksumProvider();
    }

    @Test
    void testChecksum() throws URISyntaxException {
        final Path checksumTestFile = Path.of(this.getClass().getClassLoader().getResource("checksum_test.txt")
                .toURI());

        final Mono<String> result = sha256ChecksumProvider.checksum(DOCUMENT_ID, checksumTestFile);

        StepVerifier.create(result)
                .assertNext(resultValue -> assertEquals("5be0888bbe2087f962fee5748d9cf52e37e4c6a24af79675ff7e1ca0a1b12739", resultValue))
                .verifyComplete();
    }
}
