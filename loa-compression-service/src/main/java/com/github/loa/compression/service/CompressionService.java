package com.github.loa.compression.service;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * A service that is responsible for compressing binary data.
 */
public interface CompressionService {

    /**
     * Compress the data provided on the input stream and write it to the provided output stream.
     *
     * @param uncompressedData the input stream to compress
     * @param compressedData   the output stream to write the compressed data to
     */
    void compress(InputStream uncompressedData, OutputStream compressedData);

    /**
     * Decompress the data provided on the input stream and write it to the provided output stream.
     *
     * @param compressedData   the input stream to decompress
     * @param uncompressedData the output stream to write the decompressed data to
     */
    void decompress(InputStream compressedData, OutputStream uncompressedData);
}
