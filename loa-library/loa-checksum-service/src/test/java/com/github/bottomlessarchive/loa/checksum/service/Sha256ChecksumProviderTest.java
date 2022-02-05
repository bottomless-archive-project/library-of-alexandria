package com.github.bottomlessarchive.loa.checksum.service;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class Sha256ChecksumProviderTest {

    private final Sha256ChecksumProvider sha256ChecksumProvider = new Sha256ChecksumProvider();

    @Test
    void testChecksum() throws URISyntaxException, IOException {
        final Path checksumTestFile = Path.of(Objects.requireNonNull(Thread.currentThread().getContextClassLoader()
                .getResource("checksum_test.txt")).toURI());

        final String result = sha256ChecksumProvider.checksum(
                Files.newInputStream(checksumTestFile).readAllBytes());

        assertThat(result)
                .isEqualTo("5be0888bbe2087f962fee5748d9cf52e37e4c6a24af79675ff7e1ca0a1b12739");
    }
}
