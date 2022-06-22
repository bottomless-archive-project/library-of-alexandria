package com.github.bottomlessarchive.loa.vault.service.archive;

import com.github.bottomlessarchive.loa.checksum.service.ChecksumProvider;
import com.github.bottomlessarchive.loa.compression.configuration.CompressionConfigurationProperties;
import com.github.bottomlessarchive.loa.compression.domain.DocumentCompression;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentStatus;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentType;
import com.github.bottomlessarchive.loa.document.service.entity.factory.domain.DocumentCreationContext;
import com.github.bottomlessarchive.loa.vault.service.domain.DocumentArchivingContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentCreationContextFactoryTest {

    @Mock
    private ChecksumProvider checksumProvider;

    @Mock
    private CompressionConfigurationProperties compressionConfigurationProperties;

    @InjectMocks
    private DocumentCreationContextFactory underTest;

    @Test
    void testNewContext() {
        final UUID id = UUID.randomUUID();
        final InputStream content = new ByteArrayInputStream(new byte[]{0, 1, 2, 3});
        final DocumentArchivingContext documentArchivingContext = DocumentArchivingContext.builder()
                .id(id)
                .content(content)
                .contentLength(4)
                .source("unknown")
                .type(DocumentType.PDF)
                .versionNumber(123)
                .sourceLocationId("locationId")
                .build();
        when(checksumProvider.checksum(content))
                .thenReturn("textchecksum");
        when(compressionConfigurationProperties.algorithm())
                .thenReturn(DocumentCompression.GZIP);

        final DocumentCreationContext result = underTest.newContext(documentArchivingContext);

        assertThat(result.getId(), is(id));
        assertThat(result.getFileSize(), is(4L));
        assertThat(result.getSource(), is("unknown"));
        assertThat(result.getType(), is(DocumentType.PDF));
        assertThat(result.getChecksum(), is("textchecksum"));
        assertThat(result.getVersionNumber(), is(123));
        assertThat(result.getCompression(), is(DocumentCompression.GZIP));
        assertThat(result.getStatus(), is(DocumentStatus.CREATED));
        assertThat(result.getSourceLocationId().isPresent(), is(true));
        assertThat(result.getSourceLocationId().get(), is("locationId"));
    }

    @Test
    void testNewContextWhenSourceLocationIdIsNull() {
        final UUID id = UUID.randomUUID();
        final InputStream content = new ByteArrayInputStream(new byte[]{0, 1, 2, 3});
        final DocumentArchivingContext documentArchivingContext = DocumentArchivingContext.builder()
                .id(id)
                .content(content)
                .contentLength(4)
                .source("unknown")
                .type(DocumentType.PDF)
                .versionNumber(123)
                .sourceLocationId(null)
                .build();
        when(checksumProvider.checksum(content))
                .thenReturn("textchecksum");
        when(compressionConfigurationProperties.algorithm())
                .thenReturn(DocumentCompression.GZIP);

        final DocumentCreationContext result = underTest.newContext(documentArchivingContext);

        assertThat(result.getSourceLocationId().isEmpty(), is(true));
    }
}
