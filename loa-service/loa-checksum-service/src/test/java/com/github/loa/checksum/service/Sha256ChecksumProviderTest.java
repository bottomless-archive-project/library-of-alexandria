package com.github.loa.checksum.service;

import com.github.loa.document.service.domain.DocumentType;
import com.github.loa.stage.service.StageLocationFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class Sha256ChecksumProviderTest {

    private static final String DOCUMENT_ID = "123456";
    private static final DocumentType DOCUMENT_TYPE = DocumentType.PDF;

    private StageLocationFactory stageLocationFactory;
    private Sha256ChecksumProvider sha256ChecksumProvider;

    @BeforeEach
    private void setup() {
        stageLocationFactory = mock(StageLocationFactory.class);

        sha256ChecksumProvider = new Sha256ChecksumProvider(stageLocationFactory);
    }

    @Test
    void testChecksum() {
        final File checksumTestFile = new File(this.getClass().getClassLoader().getResource("checksum_test.txt")
                .getFile());

        when(stageLocationFactory.getLocation(DOCUMENT_ID, DOCUMENT_TYPE))
                .thenReturn(Mono.just(checksumTestFile));

        final Mono<String> result = sha256ChecksumProvider.checksum(DOCUMENT_ID, DOCUMENT_TYPE);

        StepVerifier.create(result)
                .consumeNextWith(resultValue -> assertEquals("5be0888bbe2087f962fee5748d9cf52e37e4c6a24af79675ff7e1ca0a1b12739", resultValue))
                .verifyComplete();
    }
}
