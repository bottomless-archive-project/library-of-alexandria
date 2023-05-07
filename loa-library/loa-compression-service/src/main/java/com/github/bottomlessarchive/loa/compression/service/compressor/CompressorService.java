package com.github.bottomlessarchive.loa.compression.service.compressor;

import com.github.bottomlessarchive.loa.compression.domain.CompressionException;

import java.io.InputStream;
import java.nio.file.Path;

/**
 * A service that is responsible for compressing and decompressing binary data.
 */
public interface CompressorService {

    /**
     * Compress the file that is on the provided path. The result of the compression will be a file on the compressedLocation. If there was
     * a file there already it will be replaced with the result of the compression. The original file will be kept as well.
     *
     * @param originalLocation the output stream to write the compressed data to
     * @param compressedLocation the result of the compression is put on this path
     * @throws CompressionException when the compression failed
     */
    void compress(Path originalLocation, Path compressedLocation);

    /**
     * Return an {@link InputStream} that wraps the provided one with the required decompression logic.
     *
     * @param compressedData the input stream to decompress
     * @return the provided input stream wrapped with decompression logic
     * @throws CompressionException when the decompression failed
     */
    InputStream decompress(InputStream compressedData);
}
