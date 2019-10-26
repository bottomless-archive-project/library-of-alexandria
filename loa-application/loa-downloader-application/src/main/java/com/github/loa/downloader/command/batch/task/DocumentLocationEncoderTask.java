package com.github.loa.downloader.command.batch.task;

import com.github.loa.url.service.UrlEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.net.URL;

@Service
@RequiredArgsConstructor
public class DocumentLocationEncoderTask {

    private final UrlEncoder urlEncoder;

    public Mono<URL> execute(final URL location) {
        return Mono.justOrEmpty(urlEncoder.encode(location));
    }
}
