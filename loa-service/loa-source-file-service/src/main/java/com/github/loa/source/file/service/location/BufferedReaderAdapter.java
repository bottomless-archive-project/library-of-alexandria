package com.github.loa.source.file.service.location;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * An adapter method that helps to use the {@link BufferedReader} in a reactive context.
 */
@Slf4j
@Service
public class BufferedReaderAdapter {

    /**
     * Returns a function that read from a {@link BufferedReader} line by line until it's fully read.
     *
     * @return a reader function
     */
    public Function<BufferedReader, Flux<String>> consume() {
        return (reader) -> Flux.create(sink -> {
            try {
                String s;
                while (Objects.nonNull(s = reader.readLine())) {
                    sink.next(s);
                }
                sink.complete();
            } catch (final IOException e) {
                sink.error(e);
            }
        });
    }

    /**
     * Return a function that closes a {@link BufferedReader}.
     *
     * @return a closer function
     */
    public Consumer<BufferedReader> close() {
        return (reader) -> {
            try {
                reader.close();
            } catch (final IOException e) {
                throw new RuntimeException("Error while closing document location!", e);
            }
        };
    }
}
