package com.github.bottomlessarchive.loa.checksum.service;

import com.github.bottomlessarchive.loa.checksum.domain.ChecksumCalculationException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class Sha256ChecksumProviderTest {

    private final Sha256ChecksumProvider sha256ChecksumProvider = new Sha256ChecksumProvider();

    @Test
    void testChecksum() throws URISyntaxException, IOException {
        final Path checksumTestFile = Path.of(Objects.requireNonNull(Thread.currentThread().getContextClassLoader()
                .getResource("checksum_test.txt")).toURI());

        final String result = sha256ChecksumProvider.checksum(Files.newInputStream(checksumTestFile));

        assertThat(result)
                .isEqualTo("5be0888bbe2087f962fee5748d9cf52e37e4c6a24af79675ff7e1ca0a1b12739");
    }

    @Test
    void testChecksumWhenExceptionHappens() throws IOException {
        final InputStream mockInputStream = mock(InputStream.class);
        when(mockInputStream.read(any(), anyInt(), anyInt()))
                .thenThrow(new IOException());

        assertThrows(ChecksumCalculationException.class, () -> sha256ChecksumProvider.checksum(mockInputStream));
    }
}
