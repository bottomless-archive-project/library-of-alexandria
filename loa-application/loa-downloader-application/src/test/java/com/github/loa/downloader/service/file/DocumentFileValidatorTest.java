package com.github.loa.downloader.service.file;

import com.github.loa.document.service.domain.DocumentType;
import com.github.loa.downloader.configuration.DownloaderConfigurationProperties;
import com.github.loa.parser.domain.DocumentMetadata;
import com.github.loa.parser.service.DocumentDataParser;
import com.github.loa.stage.service.StageLocationFactory;
import com.github.loa.stage.service.domain.StageLocation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.InputStream;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentFileValidatorTest {

    private static final long MAX_FILE_SIZE_IN_BYTES = 2048;
    private static final UUID DOCUMENT_ID = UUID.fromString("123e4567-e89b-12d3-a456-556642440000");
    private static final DocumentType DOCUMENT_TYPE = DocumentType.DOC;

    @Mock
    private DownloaderConfigurationProperties downloaderConfigurationProperties;

    @Mock
    private DocumentDataParser documentDataParser;

    @Mock
    private StageLocationFactory stageLocationFactory;

    @InjectMocks
    private DocumentFileValidator underTest;

    @Test
    void testIsValidDocumentWhenDocumentIsTooSmall() {
        final StageLocation stageLocation = mock(StageLocation.class);

        when(stageLocationFactory.getLocation(DOCUMENT_ID.toString(), DOCUMENT_TYPE))
                .thenReturn(Mono.just(stageLocation));
        when(stageLocation.size()).thenReturn(Mono.just(0L));

        final Mono<Boolean> result = underTest.isValidDocument(DOCUMENT_ID.toString(), DOCUMENT_TYPE);

        StepVerifier.create(result)
                .consumeNextWith(Assertions::assertFalse)
                .verifyComplete();
    }

    @Test
    void testIsValidDocumentWhenDocumentIsTooBig() {
        final StageLocation stageLocation = mock(StageLocation.class);

        when(stageLocationFactory.getLocation(DOCUMENT_ID.toString(), DOCUMENT_TYPE))
                .thenReturn(Mono.just(stageLocation));
        when(stageLocation.size()).thenReturn(Mono.just(MAX_FILE_SIZE_IN_BYTES + 1));
        when(downloaderConfigurationProperties.getMaximumArchiveSize()).thenReturn(MAX_FILE_SIZE_IN_BYTES);

        final Mono<Boolean> result = underTest.isValidDocument(DOCUMENT_ID.toString(), DOCUMENT_TYPE);

        StepVerifier.create(result)
                .consumeNextWith(Assertions::assertFalse)
                .verifyComplete();
    }

    @Test
    void testIsValidDocumentWhenDocumentIsntParsable() {
        final StageLocation stageLocation = mock(StageLocation.class);

        when(stageLocationFactory.getLocation(DOCUMENT_ID.toString(), DOCUMENT_TYPE))
                .thenReturn(Mono.just(stageLocation));
        when(stageLocation.size()).thenReturn(Mono.just(MAX_FILE_SIZE_IN_BYTES - 1));
        when(downloaderConfigurationProperties.getMaximumArchiveSize()).thenReturn(MAX_FILE_SIZE_IN_BYTES);

        final InputStream documentInputStream = mock(InputStream.class);
        when(stageLocation.openStream()).thenReturn(documentInputStream);
        when(documentDataParser.parseDocumentMetadata(DOCUMENT_ID, DOCUMENT_TYPE, documentInputStream))
                .thenThrow(new RuntimeException("Some error!"));

        final Mono<Boolean> result = underTest.isValidDocument(DOCUMENT_ID.toString(), DOCUMENT_TYPE);

        StepVerifier.create(result)
                .consumeNextWith(Assertions::assertFalse)
                .verifyComplete();
        verify(documentDataParser).parseDocumentMetadata(DOCUMENT_ID, DOCUMENT_TYPE, documentInputStream);
    }

    @Test
    void testIsValidDocumentWhenDocumentIsValid() {
        final StageLocation stageLocation = mock(StageLocation.class);

        when(stageLocationFactory.getLocation(DOCUMENT_ID.toString(), DOCUMENT_TYPE))
                .thenReturn(Mono.just(stageLocation));
        when(stageLocation.size()).thenReturn(Mono.just(MAX_FILE_SIZE_IN_BYTES - 1));
        when(downloaderConfigurationProperties.getMaximumArchiveSize()).thenReturn(MAX_FILE_SIZE_IN_BYTES);

        final InputStream documentInputStream = mock(InputStream.class);
        when(stageLocation.openStream()).thenReturn(documentInputStream);
        when(documentDataParser.parseDocumentMetadata(DOCUMENT_ID, DOCUMENT_TYPE, documentInputStream))
                .thenReturn(
                        DocumentMetadata.builder()
                                .build()
                );

        final Mono<Boolean> result = underTest.isValidDocument(DOCUMENT_ID.toString(), DOCUMENT_TYPE);

        StepVerifier.create(result)
                .consumeNextWith(Assertions::assertTrue)
                .verifyComplete();
    }
}
