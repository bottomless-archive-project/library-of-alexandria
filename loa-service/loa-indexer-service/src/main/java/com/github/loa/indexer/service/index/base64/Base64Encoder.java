package com.github.loa.indexer.service.index.base64;

import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Base64;

@Service
public class Base64Encoder {

    private static final int BUFFER_SIZE = 3 * 1024;

    public String encode(final InputStream input) throws IOException {
        try (BufferedInputStream in = new BufferedInputStream(input, BUFFER_SIZE)) {
            final Base64.Encoder encoder = Base64.getEncoder();
            final StringBuilder result = new StringBuilder();
            final byte[] chunk = new byte[BUFFER_SIZE];
            final ByteRingBuffer byteBuffer = new ByteRingBuffer(BUFFER_SIZE * 2);

            int len;
            while ((len = in.read(chunk)) != -1) {
                byteBuffer.put(Arrays.copyOf(chunk, len));

                if (byteBuffer.available() >= BUFFER_SIZE) {
                    final byte[] workingChunk = new byte[BUFFER_SIZE];
                    byteBuffer.get(workingChunk);

                    result.append(encoder.encodeToString(workingChunk));
                }
            }

            if (byteBuffer.available() > 0) {
                final byte[] workingChunk = new byte[byteBuffer.available()];
                byteBuffer.get(workingChunk);

                result.append(encoder.encodeToString(workingChunk));
            }


            return result.toString();
        }
    }
}
