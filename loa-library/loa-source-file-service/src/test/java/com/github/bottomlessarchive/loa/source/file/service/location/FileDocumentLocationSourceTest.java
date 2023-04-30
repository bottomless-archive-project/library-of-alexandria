package com.github.bottomlessarchive.loa.source.file.service.location;

import com.github.bottomlessarchive.loa.location.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.location.service.factory.DocumentLocationFactory;
import com.github.bottomlessarchive.loa.source.configuration.DocumentSourceConfigurationProperties;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    private DocumentLocationFactory documentLocationFactory;

    @Mock
    private Counter processedDocumentLocationCount;

    @Mock
    private DocumentSourceConfigurationProperties documentSourceConfigurationProperties;

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
        when(documentSourceConfigurationProperties.name())
                .thenReturn("test-source");
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
        final DocumentLocation fourthDocumentLocation = DocumentLocation.builder()
                .build();
        final DocumentLocation fifthDocumentLocation = DocumentLocation.builder()
                .build();
        when(documentLocationFactory.newDocumentLocation("http://www.example.com/4", "test-source"))
                .thenReturn(Optional.of(fourthDocumentLocation));
        when(documentLocationFactory.newDocumentLocation("http://www.example.com/5", "test-source"))
                .thenReturn(Optional.of(fifthDocumentLocation));

        final List<DocumentLocation> result = underTest.streamLocations().toList();

        assertThat(result)
                .containsExactly(fourthDocumentLocation, fifthDocumentLocation);
        verify(processedDocumentLocationCount, times(2))
                .increment();
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
                                                """
                                                .toCharArray()
                                )
                        )
                );
        when(documentSourceConfigurationProperties.name())
                .thenReturn("test-source");
        final DocumentLocation firstDocumentLocation = DocumentLocation.builder()
                .build();
        final DocumentLocation secondDocumentLocation = DocumentLocation.builder()
                .build();
        final DocumentLocation thirdDocumentLocation = DocumentLocation.builder()
                .build();
        when(documentLocationFactory.newDocumentLocation("http://www.example.com/1", "test-source"))
                .thenReturn(Optional.of(firstDocumentLocation));
        when(documentLocationFactory.newDocumentLocation("http://www.example.com/2", "test-source"))
                .thenReturn(Optional.of(secondDocumentLocation));
        when(documentLocationFactory.newDocumentLocation("http://www.example.com/3", "test-source"))
                .thenReturn(Optional.of(thirdDocumentLocation));

        final List<DocumentLocation> result = underTest.streamLocations().toList();

        assertThat(result)
                .containsExactly(firstDocumentLocation, secondDocumentLocation, thirdDocumentLocation);
        verify(processedDocumentLocationCount, times(3))
                .increment();
    }
}
