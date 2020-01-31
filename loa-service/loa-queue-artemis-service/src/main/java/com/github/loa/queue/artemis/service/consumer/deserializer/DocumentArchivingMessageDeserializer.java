package com.github.loa.queue.artemis.service.consumer.deserializer;

import com.github.loa.queue.artemis.configuration.QueueServerConfiguration;
import com.github.loa.queue.service.domain.Queue;
import com.github.loa.queue.service.domain.message.DocumentArchivingMessage;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
@ConditionalOnMissingBean(QueueServerConfiguration.class)
public class DocumentArchivingMessageDeserializer implements MessageDeserializer<DocumentArchivingMessage> {

    @Override
    public DocumentArchivingMessage deserialize(final ClientMessage clientMessage) {
        final String type = clientMessage.getBodyBuffer().readString();
        final String source = clientMessage.getBodyBuffer().readString();

        final int documentContentLength = clientMessage.getBodyBuffer().readInt();

        final InputStream inputStream = new InputStream() {

            private InputStream data;
            private int dataRead = 0;

            @Override
            public int read() {
                try {
                    if (data == null || data.available() == 0) {
                        final int readableBytes = clientMessage.getBodyBuffer().readableBytes();
                        final byte[] bytes = new byte[readableBytes];

                        clientMessage.getBodyBuffer().readBytes(bytes);
                        data = new ByteArrayInputStream(bytes);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                // If read everything that was required
                if (dataRead == documentContentLength) {
                    return -1;
                }

                dataRead++;

                try {
                    return data.read();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        return DocumentArchivingMessage.builder()
                .type(type)
                .source(source)
                .contentLength(documentContentLength)
                .content(inputStream)
                .build();
    }

    @Override
    public Queue supports() {
        return Queue.DOCUMENT_ARCHIVING_QUEUE;
    }
}
