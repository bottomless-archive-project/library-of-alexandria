package com.github.loa.queue.artemis.service.consumer.deserializer;

import com.github.loa.queue.artemis.configuration.QueueServerConfiguration;
import com.github.loa.queue.service.domain.Queue;
import com.github.loa.queue.service.domain.message.DocumentArchivingMessage;
import org.apache.activemq.artemis.api.core.ActiveMQBuffer;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

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

            private ActiveMQBuffer data;
            private int dataRead = 0;

            @Override
            public int read() {
                if (data == null || !data.readable()) {
                    data = clientMessage.getBodyBuffer().readBytes(clientMessage.getBodyBuffer().readableBytes());
                }

                // If read everything that was required
                if (dataRead == documentContentLength) {
                    return -1;
                }

                dataRead++;

                return data.readByte();
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
