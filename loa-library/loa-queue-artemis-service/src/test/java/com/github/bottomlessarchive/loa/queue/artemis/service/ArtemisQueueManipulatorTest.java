package com.github.bottomlessarchive.loa.queue.artemis.service;

import com.github.bottomlessarchive.loa.queue.artemis.service.consumer.ClientConsumerExecutor;
import com.github.bottomlessarchive.loa.queue.artemis.service.consumer.deserializer.MessageDeserializer;
import com.github.bottomlessarchive.loa.queue.artemis.service.consumer.deserializer.MessageDeserializerProvider;
import com.github.bottomlessarchive.loa.queue.service.domain.Queue;
import com.github.bottomlessarchive.loa.queue.service.domain.QueueException;
import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.ActiveMQLargeMessageInterruptedException;
import org.apache.activemq.artemis.api.core.client.ClientConsumer;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArtemisQueueManipulatorTest {

    @Mock
    private ClientConsumerExecutor clientConsumerExecutor;

    @Mock
    private MessageDeserializerProvider messageDeserializerProvider;

    @Captor
    private ArgumentCaptor<Function<ClientConsumer, String>> clientConsumerArgumentCaptor;

    @InjectMocks
    private ArtemisQueueManipulator artemisQueueManipulator;

    @Test
    @SuppressWarnings("unchecked")
    void testReadMessageWhenReadWasSuccessful() throws ActiveMQException {
        when(clientConsumerExecutor.invokeConsumer(eq(Queue.DOCUMENT_ARCHIVING_QUEUE), clientConsumerArgumentCaptor.capture()))
                .thenReturn("test-result-message");

        final String result = artemisQueueManipulator.readMessage(Queue.DOCUMENT_ARCHIVING_QUEUE, String.class);

        assertThat(result)
                .isEqualTo("test-result-message");

        Function<ClientConsumer, String> resultFunction = clientConsumerArgumentCaptor.getValue();

        ClientConsumer clientConsumer = mock(ClientConsumer.class);
        ClientMessage clientMessage = mock(ClientMessage.class);
        when(clientConsumer.receive())
                .thenReturn(clientMessage);

        MessageDeserializer<String> messageDeserializer = mock(MessageDeserializer.class);
        when(messageDeserializerProvider.getDeserializer(Queue.DOCUMENT_ARCHIVING_QUEUE))
                .thenReturn(Optional.of(messageDeserializer));
        when(messageDeserializer.deserialize(clientMessage))
                .thenReturn("function-result");

        final String functionResult = resultFunction.apply(clientConsumer);

        assertThat(functionResult)
                .isEqualTo("function-result");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testReadMessageWhenLargeMessageExceptionHappen() throws ActiveMQException {
        when(clientConsumerExecutor.invokeConsumer(eq(Queue.DOCUMENT_ARCHIVING_QUEUE), clientConsumerArgumentCaptor.capture()))
                .thenReturn("test-result-message");

        final String result = artemisQueueManipulator.readMessage(Queue.DOCUMENT_ARCHIVING_QUEUE, String.class);

        assertThat(result)
                .isEqualTo("test-result-message");

        Function<ClientConsumer, String> resultFunction = clientConsumerArgumentCaptor.getValue();

        ClientConsumer clientConsumer = mock(ClientConsumer.class);
        ClientMessage clientMessage = mock(ClientMessage.class);
        when(clientConsumer.receive())
                .thenReturn(clientMessage);

        MessageDeserializer<String> messageDeserializer = mock(MessageDeserializer.class);
        when(messageDeserializerProvider.getDeserializer(Queue.DOCUMENT_ARCHIVING_QUEUE))
                .thenReturn(Optional.of(messageDeserializer));
        when(messageDeserializer.deserialize(clientMessage))
                .thenThrow(new RuntimeException("runtime-error", new ActiveMQLargeMessageInterruptedException("large-message-error")));

        assertThrows(QueueException.class, () -> resultFunction.apply(clientConsumer));
    }

    @Test
    void testReadMessageWhenActiveMQExceptionHappen() throws ActiveMQException {
        when(clientConsumerExecutor.invokeConsumer(eq(Queue.DOCUMENT_ARCHIVING_QUEUE), clientConsumerArgumentCaptor.capture()))
                .thenReturn("test-result-message");

        final String result = artemisQueueManipulator.readMessage(Queue.DOCUMENT_ARCHIVING_QUEUE, String.class);

        assertThat(result)
                .isEqualTo("test-result-message");

        Function<ClientConsumer, String> resultFunction = clientConsumerArgumentCaptor.getValue();

        ClientConsumer clientConsumer = mock(ClientConsumer.class);
        when(clientConsumer.receive())
                .thenThrow(new ActiveMQException("large-message-error"));

        assertThrows(QueueException.class, () -> resultFunction.apply(clientConsumer));
    }
}
