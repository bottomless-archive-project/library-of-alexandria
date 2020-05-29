package com.github.loa.compression.service;

import com.github.loa.compression.domain.CompressionException;

import java.io.InputStream;

/**
 * A service that is responsible for compressing binary data.
 */
public interface CompressionService {

    /**
     * Compress the data provided on the input stream and write it to the provided output stream.
     *
     * @param compressedData the output stream to write the compressed data to
     * @throws CompressionException when the compression failed
     */
    byte[] compress(byte[] compressedData);

    /**
     * Decompress the data provided on the input stream and write it to the provided output stream.
     *
     * @param compressedData the input stream to decompress
     * @throws CompressionException when the decompression failed
     */
    InputStream decompress(InputStream compressedData);
}
