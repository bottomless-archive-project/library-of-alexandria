package com.github.loa.source.service;

import reactor.core.publisher.Flux;

import java.net.URL;

/**
 * This class is streaming new URLs that should be checked for new documents.
 */
public interface DocumentLocationFactory {

    /**
     * This method is streaming new URLs that should be checked for new documents.
     *
     * @return stream of URLs that should be checked for new documents
     */
    Flux<URL> streamLocations();
}
