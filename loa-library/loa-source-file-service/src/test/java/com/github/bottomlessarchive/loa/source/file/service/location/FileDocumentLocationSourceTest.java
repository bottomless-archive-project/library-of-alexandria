package com.github.bottomlessarchive.loa.source.file.service.location;

import com.github.bottomlessarchive.loa.source.configuration.DocumentSourceConfiguration;
import com.github.bottomlessarchive.loa.location.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.source.file.configuration.FileDocumentSourceConfigurationProperties;
import com.github.bottomlessarchive.loa.source.file.service.FileSourceFactory;
import io.micrometer.core.instrument.Counter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileDocumentLocationSourceTest {

    @Mock
    private FileDocumentSourceConfigurationProperties fileDocumentSourceConfigurationProperties;

    @Mock
    private FileSourceFactory fileSourceFactory;

    @Mock
    private Counter processedDocumentLocationCount;

    @Mock
    private DocumentSourceConfiguration documentSourceConfiguration;

    @InjectMocks
    private FileDocumentLocationSource underTest;

    @Test
    void testWhenSkipLinesAreNegative() {
        when(fileDocumentSourceConfigurationProperties.skipLines())
                .thenReturn(-1L);

        assertThrows(IllegalArgumentException.class, () -> underTest.streamLocations());
    }

    @Test
    void testWhenSkipLinesAreSet() {
        when(fileDocumentSourceConfigurationProperties.skipLines())
                .thenReturn(3L);
        when(fileSourceFactory.newSourceReader())
                .thenReturn(
                        new BufferedReader(
                                new CharArrayReader(
                                        """
                                                http://www.example.com/1
                                                http://www.example.com/2
                                                http://www.example.com/3
                                                http://www.example.com/4
                                                http://www.example.com/5
                                                """
                                                .toCharArray()
                                )
                        )
                );

        final List<DocumentLocation> result = underTest.streamLocations().toList();

        assertThat(result.size(), is(2));
        verify(processedDocumentLocationCount, times(2)).increment();
    }

    @Test
    void testWhenSkipLinesAreNotSet() {
        final BufferedReader reader = mock(BufferedReader.class);
        when(fileSourceFactory.newSourceReader())
                .thenReturn(reader);
        when(fileSourceFactory.newSourceReader())
                .thenReturn(
                        new BufferedReader(
                                new CharArrayReader(
                                        """
                                                http://www.example.com/1
                                                http://www.example.com/2
                                                http://www.example.com/3
                                                http://www.example.com/4
                                                http://www.example.com/5
                                                """
                                                .toCharArray()
                                )
                        )
                );
        when(documentSourceConfiguration.getName())
                .thenReturn("test-source");

        final List<DocumentLocation> result = underTest.streamLocations().toList();

        final DocumentLocation firstDocumentLocation = result.get(0);
        assertTrue(firstDocumentLocation.getLocation().toUrl().isPresent());
        assertEquals("http://www.example.com/1", firstDocumentLocation.getLocation().toUrl().get().toString());
        assertEquals("test-source", firstDocumentLocation.getSourceName());

        final DocumentLocation secondDocumentLocation = result.get(1);
        assertTrue(secondDocumentLocation.getLocation().toUrl().isPresent());
        assertEquals("http://www.example.com/2", secondDocumentLocation.getLocation().toUrl().get().toString());
        assertEquals("test-source", secondDocumentLocation.getSourceName());

        final DocumentLocation thirdDocumentLocation = result.get(2);
        assertTrue(thirdDocumentLocation.getLocation().toUrl().isPresent());
        assertEquals("http://www.example.com/3", thirdDocumentLocation.getLocation().toUrl().get().toString());
        assertEquals("test-source", thirdDocumentLocation.getSourceName());

        final DocumentLocation fourthDocumentLocation = result.get(3);
        assertTrue(fourthDocumentLocation.getLocation().toUrl().isPresent());
        assertEquals("http://www.example.com/4", fourthDocumentLocation.getLocation().toUrl().get().toString());
        assertEquals("test-source", fourthDocumentLocation.getSourceName());

        final DocumentLocation fifthDocumentLocation = result.get(4);
        assertTrue(fifthDocumentLocation.getLocation().toUrl().isPresent());
        assertEquals("http://www.example.com/5", fifthDocumentLocation.getLocation().toUrl().get().toString());
        assertEquals("test-source", fifthDocumentLocation.getSourceName());

        verify(processedDocumentLocationCount, times(5)).increment();
    }
}
