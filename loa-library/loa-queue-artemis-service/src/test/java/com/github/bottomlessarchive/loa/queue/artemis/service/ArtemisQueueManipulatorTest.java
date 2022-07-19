package com.github.bottomlessarchive.loa.queue.artemis.service;

import com.github.bottomlessarchive.loa.queue.artemis.service.consumer.deserializer.MessageDeserializer;
import com.github.bottomlessarchive.loa.queue.artemis.service.consumer.deserializer.MessageDeserializerProvider;
import com.github.bottomlessarchive.loa.queue.artemis.service.consumer.pool.QueueConsumerProvider;
import com.github.bottomlessarchive.loa.queue.artemis.service.consumer.pool.domain.QueueConsumer;
import com.github.bottomlessarchive.loa.queue.service.domain.Queue;
import com.github.bottomlessarchive.loa.queue.service.domain.QueueException;
import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.client.ClientConsumer;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArtemisQueueManipulatorTest {

    @Mock
    private MessageDeserializerProvider messageDeserializerProvider;

    @Mock
    private QueueConsumerProvider queueConsumerProvider;

    @InjectMocks
    private ArtemisQueueManipulator artemisQueueManipulator;

    @Test
    @SuppressWarnings("unchecked")
    void testReadMessageWhenReadWasSuccessful() throws ActiveMQException {
        final ClientConsumer clientConsumer = mock(ClientConsumer.class);
        final QueueConsumer queueConsumer = QueueConsumer.builder()
                .clientConsumer(clientConsumer)
                .build();
        when(queueConsumerProvider.getConsumer(Queue.DOCUMENT_ARCHIVING_QUEUE))
                .thenReturn(queueConsumer);
        final ClientMessage clientMessage = mock(ClientMessage.class);
        when(clientConsumer.receive())
                .thenReturn(clientMessage);

        final MessageDeserializer<String> messageDeserializer = mock(MessageDeserializer.class);
        when(messageDeserializerProvider.getDeserializer(Queue.DOCUMENT_ARCHIVING_QUEUE))
                .thenReturn(Optional.of(messageDeserializer));
        when(messageDeserializer.deserialize(clientMessage))
                .thenReturn("function-result");

        final String result = artemisQueueManipulator.readMessage(Queue.DOCUMENT_ARCHIVING_QUEUE, String.class);

        assertThat(result)
                .isEqualTo("function-result");
    }

    @Test
    void testReadMessageWhenActiveMQExceptionHappen() throws ActiveMQException {
        final ClientConsumer clientConsumer = mock(ClientConsumer.class);
        final QueueConsumer queueConsumer = QueueConsumer.builder()
                .clientConsumer(clientConsumer)
                .build();
        when(queueConsumerProvider.getConsumer(Queue.DOCUMENT_ARCHIVING_QUEUE))
                .thenReturn(queueConsumer);
        when(clientConsumer.receive())
                .thenThrow(new ActiveMQException("large-message-error"));

        assertThrows(QueueException.class, () -> artemisQueueManipulator.readMessage(Queue.DOCUMENT_ARCHIVING_QUEUE, String.class));
    }
}
