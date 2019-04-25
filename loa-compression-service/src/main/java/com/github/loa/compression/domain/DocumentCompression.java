package com.github.loa.compression.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DocumentCompression {

    NONE("pdf"),
    GZIP("gz"),
    LZMA("lzma");

    private final String extension;
}
