package com.github.bottomlessarchive.loa.queue.artemis.service;

import com.github.bottomlessarchive.loa.queue.artemis.service.consumer.deserializer.MessageDeserializer;
import com.github.bottomlessarchive.loa.queue.artemis.service.consumer.deserializer.MessageDeserializerProvider;
import com.github.bottomlessarchive.loa.queue.artemis.service.consumer.pool.QueueConsumerProvider;
import com.github.bottomlessarchive.loa.queue.artemis.service.consumer.pool.domain.QueueConsumer;
import com.github.bottomlessarchive.loa.queue.artemis.service.producer.pool.QueueProducerProvider;
import com.github.bottomlessarchive.loa.queue.artemis.service.producer.pool.domain.QueueProducer;
import com.github.bottomlessarchive.loa.queue.artemis.service.producer.serializer.MessageSerializer;
import com.github.bottomlessarchive.loa.queue.artemis.service.producer.serializer.MessageSerializerProvider;
import com.github.bottomlessarchive.loa.queue.service.domain.Queue;
import com.github.bottomlessarchive.loa.queue.service.domain.QueueException;
import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.client.ClientConsumer;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.apache.activemq.artemis.api.core.client.ClientProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArtemisQueueManipulatorTest {

    @Mock
    private MessageSerializerProvider messageSerializerProvider;

    @Mock
    private MessageDeserializerProvider messageDeserializerProvider;

    @Mock
    private QueueConsumerProvider queueConsumerProvider;

    @Mock
    private QueueProducerProvider queueProducerProvider;

    @InjectMocks
    private ArtemisQueueManipulator underTest;

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
        when(clientConsumer.receive(100))
                .thenReturn(clientMessage);

        final MessageDeserializer<String> messageDeserializer = mock(MessageDeserializer.class);
        when(messageDeserializerProvider.getDeserializer(Queue.DOCUMENT_ARCHIVING_QUEUE))
                .thenReturn(Optional.of(messageDeserializer));
        when(messageDeserializer.deserialize(clientMessage))
                .thenReturn("function-result");

        final Optional<String> result = underTest.readMessage(Queue.DOCUMENT_ARCHIVING_QUEUE, String.class);

        assertThat(result)
                .isPresent()
                .contains("function-result");
    }

    @Test
    void testReadMessageWhenActiveMQExceptionHappen() throws ActiveMQException {
        final ClientConsumer clientConsumer = mock(ClientConsumer.class);
        final QueueConsumer queueConsumer = QueueConsumer.builder()
                .clientConsumer(clientConsumer)
                .build();
        when(queueConsumerProvider.getConsumer(Queue.DOCUMENT_ARCHIVING_QUEUE))
                .thenReturn(queueConsumer);
        when(clientConsumer.receive(100))
                .thenThrow(new ActiveMQException("large-message-error"));

        assertThrows(QueueException.class, () -> underTest.readMessage(Queue.DOCUMENT_ARCHIVING_QUEUE, String.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testSendMessageWhenSendWasSuccessful() throws ActiveMQException {
        final MessageSerializer<Object> messageSerializer = mock(MessageSerializer.class);
        when(messageSerializerProvider.getSerializer(Queue.DOCUMENT_ARCHIVING_QUEUE))
                .thenReturn(Optional.of(messageSerializer));
        final Object message = new Object();
        final ClientMessage clientMessage = mock(ClientMessage.class);
        when(messageSerializer.serialize(message))
                .thenReturn(clientMessage);
        final ClientProducer clientProducer = mock(ClientProducer.class);
        final QueueProducer queueProducer = QueueProducer.builder()
                .clientProducer(clientProducer)
                .build();
        when(queueProducerProvider.getProducer(Queue.DOCUMENT_ARCHIVING_QUEUE))
                .thenReturn(queueProducer);

        underTest.sendMessage(Queue.DOCUMENT_ARCHIVING_QUEUE, message);

        verify(clientProducer).send(clientMessage);
    }

    @Test
    void testSendMessageWhenMessageSerializerWasNotFound() {
        when(messageSerializerProvider.getSerializer(Queue.DOCUMENT_ARCHIVING_QUEUE))
                .thenReturn(Optional.empty());

        assertThrows(QueueException.class, () -> underTest.sendMessage(Queue.DOCUMENT_ARCHIVING_QUEUE, new Object()));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testSendMessageWhenSendWasUnsuccessful() throws ActiveMQException {
        final MessageSerializer<Object> messageSerializer = mock(MessageSerializer.class);
        when(messageSerializerProvider.getSerializer(Queue.DOCUMENT_ARCHIVING_QUEUE))
                .thenReturn(Optional.of(messageSerializer));
        final Object message = new Object();
        final ClientMessage clientMessage = mock(ClientMessage.class);
        when(messageSerializer.serialize(message))
                .thenReturn(clientMessage);
        final ClientProducer clientProducer = mock(ClientProducer.class);
        final QueueProducer queueProducer = QueueProducer.builder()
                .clientProducer(clientProducer)
                .build();
        when(queueProducerProvider.getProducer(Queue.DOCUMENT_ARCHIVING_QUEUE))
                .thenReturn(queueProducer);
        doThrow(new QueueException("Test exception."))
                .when(clientProducer)
                .send(clientMessage);

        assertThrows(QueueException.class, () -> underTest.sendMessage(Queue.DOCUMENT_ARCHIVING_QUEUE, message));
    }
}
