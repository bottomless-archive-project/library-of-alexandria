package com.github.bottomlessarchive.loa.downloader.service.file;

import com.github.bottomlessarchive.loa.document.service.domain.DocumentType;
import com.github.bottomlessarchive.loa.file.FileManipulatorService;
import com.github.bottomlessarchive.loa.url.service.downloader.FileDownloadManager;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FileCollectorTest {

    private static final Path TEST_PATH = Path.of("/test.pdf");

    @Mock
    private FileDownloadManager fileDownloadManager;

    @Mock
    private FileManipulatorService fileManipulatorService;

    @InjectMocks
    private FileCollector underTest;

    @Test
    void testWhenHttpLocationIsVisited() throws MalformedURLException {
        final URL testLocation = new URL("http://localhost/test.pdf");

        underTest.acquireFile(testLocation, TEST_PATH, DocumentType.PDF);

        verify(fileDownloadManager)
                .downloadFile(testLocation, TEST_PATH);
    }

    @Test
    void testWhenHttpsLocationIsVisited() throws MalformedURLException {
        final URL testLocation = new URL("http://localhost/test.pdf");

        underTest.acquireFile(testLocation, TEST_PATH, DocumentType.PDF);

        verify(fileDownloadManager)
                .downloadFile(testLocation, TEST_PATH);
    }

    @Test
    void testWhenFileLocationIsVisited() throws IOException, URISyntaxException {
        final URL testLocation = new URL("file:\\C:\\Test\\test.pdf");

        underTest.acquireFile(testLocation, TEST_PATH, DocumentType.PDF);

        verify(fileManipulatorService)
                .copy(Path.of(testLocation.toURI()), TEST_PATH);
    }

    @Test
    @Disabled
    void testWhenFB2ArchiveIsAcquired() throws MalformedURLException {
        final URL testLocation = new URL("http://localhost/test.fb2.zip");

        underTest.acquireFile(testLocation, TEST_PATH, DocumentType.FB2);

        verify(fileDownloadManager)
                .downloadFile(testLocation, TEST_PATH);

        //TODO: Figure out something to test this scenario regardless of the complexity around the filesystem operations
    }
}
