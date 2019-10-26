package com.github.loa.downloader.command.batch;

import reactor.core.publisher.Flux;

public interface DocumentLocationFactory {

    Flux<String> streamLocations();
}
