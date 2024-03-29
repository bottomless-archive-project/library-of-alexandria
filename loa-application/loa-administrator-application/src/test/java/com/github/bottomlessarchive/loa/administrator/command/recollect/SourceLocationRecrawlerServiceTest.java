package com.github.bottomlessarchive.loa.administrator.command.recollect;

import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.io.service.downloader.FileDownloadManager;
import com.github.bottomlessarchive.loa.location.service.factory.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.stage.service.StageLocationFactory;
import com.github.bottomlessarchive.loa.stage.service.domain.StageLocation;
import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import com.github.bottomlessarchive.loa.validator.service.DocumentFileValidator;
import com.github.bottomlessarchive.loa.vault.client.service.VaultClientService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
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
        final DocumentLocation documentLocation = DocumentLocation.builder()
                .id("test-id")
                .url("https://example.com/")
                .build();
        final DocumentEntity documentEntity = DocumentEntity.builder()
                .type(DocumentType.PDF)
                .build();

        final StageLocation stageLocation = mock(StageLocation.class);
        when(stageLocationFactory.getLocation(any()))
                .thenReturn(stageLocation);

        final Path mockPath = mock(Path.class);
        when(stageLocation.getPath())
                .thenReturn(mockPath);

        when(documentFileValidator.isValidDocument(any(), eq(stageLocation), eq(DocumentType.PDF)))
                .thenReturn(true);

        final byte[] content = {1, 2, 3};
        when(stageLocation.openStream())
                .thenReturn(new ByteArrayInputStream(content));

        underTest.recrawlSourceLocation(documentLocation, documentEntity);

        verify(stageLocation).close();
        verify(fileDownloadManager).downloadFile(eq("https://example.com/"), eq(mockPath));
        verify(vaultClientService).replaceCorruptDocument(documentEntity, content);
    }
}
