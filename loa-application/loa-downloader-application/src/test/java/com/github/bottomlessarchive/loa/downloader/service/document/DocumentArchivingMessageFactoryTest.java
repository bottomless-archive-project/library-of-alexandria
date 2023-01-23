package com.github.bottomlessarchive.loa.downloader.service.document;

import com.github.bottomlessarchive.loa.checksum.service.ChecksumProvider;
import com.github.bottomlessarchive.loa.compression.configuration.CompressionConfigurationProperties;
import com.github.bottomlessarchive.loa.compression.domain.DocumentCompression;
import com.github.bottomlessarchive.loa.downloader.service.document.domain.DocumentArchivingContext;
import com.github.bottomlessarchive.loa.file.FileManipulatorService;
import com.github.bottomlessarchive.loa.queue.service.domain.message.DocumentArchivingMessage;
import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentArchivingMessageFactoryTest {

    @Mock
    private ChecksumProvider checksumProvider;

    @Mock
    private FileManipulatorService fileManipulatorService;

    @Mock
    private CompressionConfigurationProperties compressionConfigurationProperties;

    @InjectMocks
    private DocumentArchivingMessageFactory documentArchivingMessageFactory;

    @Test
    @SneakyThrows
    void testNewDocumentArchivingMessage() {
        final UUID documentId = UUID.randomUUID();
        final Path content = mock(Path.class);
        final DocumentArchivingContext documentArchivingContext = DocumentArchivingContext.builder()
                .id(documentId)
                .type(DocumentType.DOC)
                .source("test-source")
                .sourceLocationId("test-source-location-id")
                .contents(content)
                .build();
        when(fileManipulatorService.size(content))
                .thenReturn(8L);
        final Path compressedContent = mock(Path.class);
        when(fileManipulatorService.size(compressedContent))
                .thenReturn(10L);
        final InputStream contentInputStream = mock(InputStream.class);
        when(fileManipulatorService.getInputStream(content))
                .thenReturn(contentInputStream);
        when(checksumProvider.checksum(contentInputStream))
                .thenReturn("test-checksum");
        when(compressionConfigurationProperties.algorithm())
                .thenReturn(DocumentCompression.GZIP);

        final DocumentArchivingMessage result = documentArchivingMessageFactory.newDocumentArchivingMessage(
                documentArchivingContext, compressedContent);

        assertThat(result.id())
                .isEqualTo(documentId.toString());
        assertThat(result.type())
                .isEqualTo("DOC");
        assertThat(result.source())
                .isEqualTo("test-source");
        assertThat(result.sourceLocationId())
                .isPresent()
                .hasValue("test-source-location-id");
        assertThat(result.contentLength())
                .isEqualTo(10L);
        assertThat(result.originalContentLength())
                .isEqualTo(8L);
        assertThat(result.checksum())
                .isEqualTo("test-checksum");
        assertThat(result.compression())
                .isEqualTo("GZIP");
    }
}
