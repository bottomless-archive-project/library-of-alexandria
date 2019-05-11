package com.github.loa.compression.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Describes what kind of compression is used to store a document.
 */
@Getter
@RequiredArgsConstructor
public enum DocumentCompression {

    /**
     * No compression used.
     */
    NONE(""),

    /**
     * Using the <a href="https://en.wikipedia.org/wiki/Gzip">GZIP</a> compression algorithm. It is quite quick but less
     * efficient than {@link DocumentCompression#LZMA}.
     */
    GZIP("gz"),

    /**
     * <a href="https://en.wikipedia.org/wiki/Lzma">LZMA</a> is the most efficient compression algorithm but it is
     * very slow.
     */
    LZMA("lzma");

    /**
     * The file extension used for the compressed archived documents.
     */
    private final String fileExtension;
}
