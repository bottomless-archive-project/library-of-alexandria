package com.github.bottomlessarchive.loa.io.service.collector;

import com.github.bottomlessarchive.loa.file.FileManipulatorService;
import com.github.bottomlessarchive.loa.file.zip.ZipFileManipulatorService;
import com.github.bottomlessarchive.loa.io.service.downloader.FileDownloadManager;
import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentCollectorTest {

    private static final Path TEST_PATH = Path.of("/test.pdf");

    @Mock
    private FileDownloadManager fileDownloadManager;

    @Mock
    private FileManipulatorService fileManipulatorService;

    @Mock
    private ZipFileManipulatorService zipFileManipulatorService;

    @InjectMocks
    private DocumentCollector underTest;

    @Test
    void testWhenHttpLocationIsVisited() throws MalformedURLException {
        final URL testLocation = new URL("http://localhost/test.pdf");

        underTest.acquireDocument(testLocation, TEST_PATH, DocumentType.PDF);

        verify(fileDownloadManager)
                .downloadFile(testLocation, TEST_PATH);
    }

    @Test
    void testWhenHttpsLocationIsVisited() throws MalformedURLException {
        final URL testLocation = new URL("http://localhost/test.pdf");

        underTest.acquireDocument(testLocation, TEST_PATH, DocumentType.PDF);

        verify(fileDownloadManager)
                .downloadFile(testLocation, TEST_PATH);
    }

    @Test
    void testWhenFileLocationIsVisited() throws IOException, URISyntaxException {
        final URL testLocation = new URL("file:" + System.getProperty("java.io.tmpdir") + "//test.pdf");

        underTest.acquireDocument(testLocation, TEST_PATH, DocumentType.PDF);

        verify(fileManipulatorService)
                .copy(testLocation.toURI(), TEST_PATH);
    }

    @Test
    void testWhenFB2ArchiveIsAcquired() throws IOException {
        final URL testLocation = new URL("http://localhost/test.fb2.zip");

        when(zipFileManipulatorService.isZipArchive(any()))
                .thenReturn(true);

        underTest.acquireDocument(testLocation, TEST_PATH, DocumentType.FB2);

        verify(fileDownloadManager)
                .downloadFile(testLocation, TEST_PATH);

        final InOrder order = Mockito.inOrder(zipFileManipulatorService, fileManipulatorService);
        order.verify(zipFileManipulatorService)
                .unzipSingleFileArchive(TEST_PATH, Path.of("/test.pdf.tmp"));
        order.verify(fileManipulatorService)
                .move(Path.of("/test.pdf.tmp"), TEST_PATH, StandardCopyOption.REPLACE_EXISTING);
    }
}
