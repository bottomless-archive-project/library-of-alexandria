package com.github.loa.vault.service;

import com.github.loa.checksum.service.ChecksumProvider;
import com.github.loa.compression.configuration.CompressionConfigurationProperties;
import com.github.loa.compression.domain.DocumentCompression;
import com.github.loa.document.service.domain.DocumentStatus;
import com.github.loa.document.service.domain.DocumentType;
import com.github.loa.document.service.entity.factory.domain.DocumentCreationContext;
import com.github.loa.vault.configuration.VaultConfigurationProperties;
import com.github.loa.vault.service.archive.DocumentCreationContextFactory;
import com.github.loa.vault.service.domain.DocumentArchivingContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentCreationContextFactoryTest {

    @Mock
    private ChecksumProvider checksumProvider;

    @Mock
    private VaultConfigurationProperties vaultConfigurationProperties;

    @Mock
    private CompressionConfigurationProperties compressionConfigurationProperties;

    @InjectMocks
    private DocumentCreationContextFactory underTest;

    @Test
    void testNewContext() {
        final UUID id = UUID.randomUUID();
        final byte[] content = new byte[]{0, 1, 2, 3};
        final DocumentArchivingContext documentArchivingContext = DocumentArchivingContext.builder()
                .id(id)
                .content(content)
                .contentLength(4)
                .source("unknown")
                .type(DocumentType.PDF)
                .build();
        when(checksumProvider.checksum(content))
                .thenReturn(Mono.just("textchecksum"));
        when(vaultConfigurationProperties.getVersionNumber())
                .thenReturn(123);
        when(compressionConfigurationProperties.getAlgorithm())
                .thenReturn(DocumentCompression.GZIP);

        final Mono<DocumentCreationContext> result = underTest.newContext(documentArchivingContext);

        StepVerifier.create(result)
                .consumeNextWith(documentCreationContext -> {
                    assertThat(documentCreationContext.getId(), is(id));
                    assertThat(documentCreationContext.getFileSize(), is(4L));
                    assertThat(documentCreationContext.getSource(), is("unknown"));
                    assertThat(documentCreationContext.getType(), is(DocumentType.PDF));
                    assertThat(documentCreationContext.getChecksum(), is("textchecksum"));
                    assertThat(documentCreationContext.getVersionNumber(), is(123));
                    assertThat(documentCreationContext.getCompression(), is(DocumentCompression.GZIP));
                    assertThat(documentCreationContext.getStatus(), is(DocumentStatus.DOWNLOADED));
                })
                .verifyComplete();
    }
}