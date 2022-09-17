package com.github.bottomlessarchive.loa.source.file.service.location;

import com.github.bottomlessarchive.loa.location.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.location.service.id.factory.DocumentLocationIdFactory;
import com.github.bottomlessarchive.loa.source.configuration.DocumentSourceConfiguration;
import com.github.bottomlessarchive.loa.source.file.configuration.FileDocumentSourceConfigurationProperties;
import com.github.bottomlessarchive.loa.source.file.service.FileSourceFactory;
import com.github.bottomlessarchive.loa.type.DocumentTypeCalculator;
import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import com.github.bottomlessarchive.loa.url.service.encoder.UrlEncoder;
import io.micrometer.core.instrument.Counter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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

    @Mock
    private UrlEncoder urlEncoder;

    @Mock
    private DocumentTypeCalculator documentTypeCalculator;

    @Mock
    private DocumentLocationIdFactory documentLocationIdFactory;

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
        when(urlEncoder.encode(any()))
                .thenAnswer(invocation -> Optional.of(invocation.getArguments()[0]));
        when(documentTypeCalculator.calculate(any()))
                .thenReturn(Optional.of(DocumentType.DOC));
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

        assertThat(result.size())
                .isEqualTo(2);
        verify(processedDocumentLocationCount, times(2)).increment();
    }

    @Test
    void testWhenSkipLinesAreNotSet() throws MalformedURLException {
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
        when(documentSourceConfiguration.getName())
                .thenReturn("test-source");
        when(urlEncoder.encode(any()))
                .thenAnswer(invocation -> Optional.of(invocation.getArguments()[0]));
        when(documentTypeCalculator.calculate(any()))
                .thenReturn(Optional.of(DocumentType.DOC));
        when(documentLocationIdFactory.newDocumentLocationId(new URL("http://www.example.com/1")))
                .thenReturn("123");
        when(documentLocationIdFactory.newDocumentLocationId(new URL("http://www.example.com/2")))
                .thenReturn("456");
        when(documentLocationIdFactory.newDocumentLocationId(new URL("http://www.example.com/3")))
                .thenReturn("789");

        final List<DocumentLocation> result = underTest.streamLocations().toList();

        assertThat(result)
                .element(0)
                .satisfies(element -> {
                    assertThat(element.getId())
                            .isEqualTo("123");
                    assertThat(element.getLocation())
                            .isEqualTo(new URL("http://www.example.com/1"));
                    assertThat(element.getSourceName())
                            .isEqualTo("test-source");
                    assertThat(element.getType())
                            .isEqualTo(DocumentType.DOC);
                });

        assertThat(result)
                .element(1)
                .satisfies(element -> {
                    assertThat(element.getId())
                            .isEqualTo("456");
                    assertThat(element.getLocation())
                            .isEqualTo(new URL("http://www.example.com/2"));
                    assertThat(element.getSourceName())
                            .isEqualTo("test-source");
                    assertThat(element.getType())
                            .isEqualTo(DocumentType.DOC);
                });

        assertThat(result)
                .element(2)
                .satisfies(element -> {
                    assertThat(element.getId())
                            .isEqualTo("789");
                    assertThat(element.getLocation())
                            .isEqualTo(new URL("http://www.example.com/3"));
                    assertThat(element.getSourceName())
                            .isEqualTo("test-source");
                    assertThat(element.getType())
                            .isEqualTo(DocumentType.DOC);
                });

        verify(processedDocumentLocationCount, times(3)).increment();
    }
}
