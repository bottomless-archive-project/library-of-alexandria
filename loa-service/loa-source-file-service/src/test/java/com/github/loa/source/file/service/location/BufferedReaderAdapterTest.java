package com.github.loa.source.file.service.location;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.BufferedReader;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BufferedReaderAdapterTest {

    private final BufferedReaderAdapter bufferedReaderAdapter = new BufferedReaderAdapter();

    @Mock
    private BufferedReader bufferedReaderMock;

    @Test
    @SneakyThrows
    public void testCloseWhenNoExceptionThrown() {
        bufferedReaderAdapter.close().accept(bufferedReaderMock);

        verify(bufferedReaderMock, only()).close();
    }

    @Test
    @SneakyThrows
    public void testCloseWhenExceptionThrown() {
        doThrow(new RuntimeException()).when(bufferedReaderMock).close();

        assertThrows(RuntimeException.class,
                () -> bufferedReaderAdapter.close().accept(bufferedReaderMock));
    }

    @Test
    @SneakyThrows
    public void testConsumeReturnsLinesCorrectly() {
        when(bufferedReaderMock.readLine()).thenReturn("aaa", "bbb", "ccc", null);

        final Flux<String> result = bufferedReaderAdapter.consume().apply(bufferedReaderMock);

        StepVerifier.create(result)
                .consumeNextWith(a -> assertThat(a, is("aaa")))
                .consumeNextWith(a -> assertThat(a, is("bbb")))
                .consumeNextWith(a -> assertThat(a, is("ccc")))
                .verifyComplete();
    }

    @Test
    @SneakyThrows
    public void testConsumeThrowsException() {
        when(bufferedReaderMock.readLine())
                .thenReturn("aaa")
                .thenThrow(new IOException());

        final Flux<String> result = bufferedReaderAdapter.consume().apply(bufferedReaderMock);

        StepVerifier.create(result)
                .consumeNextWith(a -> assertThat(a, is("aaa")))
                .verifyError();
    }
}