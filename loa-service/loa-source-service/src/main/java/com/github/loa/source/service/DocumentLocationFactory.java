package com.github.loa.source.service;

import reactor.core.publisher.Flux;

import java.net.URL;

public interface DocumentLocationFactory {

    Flux<URL> streamLocations();
}
