package com.github.bottomlessarchive.loa.checksum.service;

import com.github.bottomlessarchive.loa.checksum.domain.ChecksumCalculationException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

/**
 * A {@link ChecksumProvider} that uses SHA-256 encoding to calculate a checksum for the provided input.
 *
 * @see <a href="https://en.wikipedia.org/wiki/SHA-2">https://en.wikipedia.org/wiki/SHA-2</a>
 * @see <a href="https://en.wikipedia.org/wiki/Checksum">https://en.wikipedia.org/wiki/Checksum</a>
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "loa.checksum.type", havingValue = "sha256")
public class Sha256ChecksumProvider implements ChecksumProvider {

    /**
     * Generate a checksum for the provided document using the SHA-256 algorithm.
     *
     * @param documentContents the contents of the document
     * @return the checksum for the document
     */
    @Override
    public String checksum(final InputStream documentContents) {
        try {
            return DigestUtils.sha256Hex(documentContents);
        } catch (IOException e) {
            throw new ChecksumCalculationException("Failed to calculate SHA-256 checksum!", e);
        }
    }
}
