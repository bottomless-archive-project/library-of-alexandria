package com.github.loa.source.file.service.location;

import com.github.loa.location.domain.DocumentLocation;
import com.github.loa.source.file.configuration.FileDocumentSourceConfigurationProperties;
import com.github.loa.source.file.service.FileSourceFactory;
import com.github.loa.url.service.URLConverter;
import io.micrometer.core.instrument.Counter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.BufferedReader;
import java.net.URL;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileDocumentLocationFactoryTest {

    @Mock
    private FileDocumentSourceConfigurationProperties fileDocumentSourceConfigurationProperties;

    @Mock
    private FileSourceFactory fileSourceFactory;

    @Mock
    private URLConverter urlConverter;

    @Mock
    private Counter processedDocumentLocationCount;

    @Mock
    private BufferedReaderAdapter adapter;

    @InjectMocks
    private FileDocumentLocationSource underTest;

    @Test
    void testWhenSkipLinesAreNegative() {
        when(fileDocumentSourceConfigurationProperties.getSkipLines())
                .thenReturn(-1);

        assertThrows(IllegalArgumentException.class, () -> underTest.streamLocations());
    }

    @Test
    void testWhenSkipLinesAreSet() {
        when(fileDocumentSourceConfigurationProperties.getSkipLines())
                .thenReturn(3);
        final BufferedReader reader = mock(BufferedReader.class);
        when(fileSourceFactory.newSourceReader())
                .thenReturn(reader);
        when(urlConverter.convert(anyString()))
                .thenAnswer((Answer<Mono<URL>>) invocation -> Mono.just(new URL(invocation.getArgument(0))));
        when(adapter.consume())
                .thenReturn((newReader) -> {
                    assertThat(reader, is(newReader));

                    return Flux.fromIterable(List.of("http://www.example.com/1", "http://www.example.com/2", "http://www.example.com/3",
                            "http://www.example.com/4", "http://www.example.com/5"));
                });
        when(adapter.close())
                .thenReturn((newReader) -> {
                });

        final Flux<DocumentLocation> result = underTest.streamLocations();

        StepVerifier.create(result)
                .expectNextCount(2)
                .verifyComplete();

        verify(processedDocumentLocationCount, times(2)).increment();
    }

    @Test
    void testWhenSkipLinesAreNotSet() {
        final BufferedReader reader = mock(BufferedReader.class);
        when(fileSourceFactory.newSourceReader())
                .thenReturn(reader);
        when(urlConverter.convert(anyString()))
                .thenAnswer((Answer<Mono<URL>>) invocation -> Mono.just(new URL(invocation.getArgument(0))));
        when(adapter.consume())
                .thenReturn((newReader) -> {
                    assertThat(reader, is(newReader));

                    return Flux.fromIterable(List.of("http://www.example.com/1", "http://www.example.com/2", "http://www.example.com/3",
                            "http://www.example.com/4", "http://www.example.com/5"));
                });
        when(adapter.close())
                .thenReturn((newReader) -> {
                });

        final Flux<DocumentLocation> result = underTest.streamLocations();

        StepVerifier.create(result)
                .consumeNextWith(url -> assertEquals("http://www.example.com/1", url.getLocation().toString()))
                .consumeNextWith(url -> assertEquals("http://www.example.com/2", url.getLocation().toString()))
                .consumeNextWith(url -> assertEquals("http://www.example.com/3", url.getLocation().toString()))
                .consumeNextWith(url -> assertEquals("http://www.example.com/4", url.getLocation().toString()))
                .consumeNextWith(url -> assertEquals("http://www.example.com/5", url.getLocation().toString()))
                .verifyComplete();

        verify(processedDocumentLocationCount, times(5)).increment();
    }
}