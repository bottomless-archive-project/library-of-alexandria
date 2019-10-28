package com.github.loa.downloader.command.batch;

import reactor.core.publisher.Flux;

import java.net.URL;

public interface DocumentLocationFactory {

    Flux<URL> streamLocations();
}
