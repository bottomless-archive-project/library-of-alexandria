package com.github.loa.queue.artemis.service.consumer.deserializer.common;

import lombok.RequiredArgsConstructor;
import org.apache.activemq.artemis.api.core.ActiveMQBuffer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@RequiredArgsConstructor
public class ArtemisInputStream extends InputStream {

    private final int contentLength;
    private final ActiveMQBuffer content;

    private InputStream data;
    private int dataRead = 0;

    @Override
    public int read() {
        try {
            if (data == null || data.available() == 0) {
                final int readableBytes = content.readableBytes();
                final byte[] bytes = new byte[readableBytes];

                content.readBytes(bytes);
                data = new ByteArrayInputStream(bytes);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // If read everything that was required
        if (dataRead == contentLength) {
            return -1;
        }

        dataRead++;

        try {
            return data.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
