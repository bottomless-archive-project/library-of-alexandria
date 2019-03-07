package com.github.loa.downloader.source.service;

import java.net.URL;
import java.util.stream.Stream;

public interface DocumentSourceProvider {

    Stream<URL> stream();
}
