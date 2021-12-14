package com.github.bottomlessarchive.loa.administrator.command.recollect;

import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentType;
import com.github.bottomlessarchive.loa.stage.service.StageLocationFactory;
import com.github.bottomlessarchive.loa.stage.service.domain.StageLocation;
import com.github.bottomlessarchive.loa.url.service.downloader.FileDownloadManager;
import com.github.bottomlessarchive.loa.validator.service.DocumentFileValidator;
import com.github.bottomlessarchive.loa.vault.client.service.VaultClientService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SourceLocationRecrawlerServiceTest {

    @Mock
    private StageLocationFactory stageLocationFactory;

    @Mock
    private DocumentFileValidator documentFileValidator;

    @Mock
    private VaultClientService vaultClientService;

    @Mock
    private FileDownloadManager fileDownloadManager;

    @InjectMocks
    private SourceLocationRecrawlerService underTest;

    @Test
    void testRecrawlSourceLocationWhenSourceIsDownloadabe() throws MalformedURLException {
        final URL sourceLocation = new URL("http://example.com/");
        final DocumentEntity documentEntity = DocumentEntity.builder()
                .type(DocumentType.PDF)
                .build();

        final StageLocation stageLocation = mock(StageLocation.class);
        when(stageLocation.exists())
                .thenReturn(Mono.just(true));
        when(stageLocationFactory.getLocation(any(), eq(DocumentType.PDF)))
                .thenReturn(Mono.just(stageLocation));

        final Path mockPath = mock(Path.class);
        when(stageLocation.getPath())
                .thenReturn(mockPath);
        when(fileDownloadManager.downloadFile(sourceLocation, mockPath))
                .thenReturn(Mono.just(mockPath));

        when(documentFileValidator.isValidDocument(any(), eq(DocumentType.PDF)))
                .thenReturn(Mono.just(true));

        final byte[] content = new byte[]{1, 2, 3};
        when(stageLocation.openStream())
                .thenReturn(new ByteArrayInputStream(content));
        when(vaultClientService.replaceCorruptDocument(documentEntity, content))
                .thenReturn(Mono.empty());

        Mono<DocumentEntity> result = underTest.recrawlSourceLocation(sourceLocation, documentEntity);

        StepVerifier.create(result)
                .consumeNextWith(localDocumentEntity -> Assertions.assertEquals(documentEntity, localDocumentEntity))
                .verifyComplete();

        verify(stageLocation).cleanup();
        verify(vaultClientService).replaceCorruptDocument(documentEntity, content);
    }
}
