package com.github.bottomlessarchive.loa.queue.artemis.service.producer.serializer;

import com.github.bottomlessarchive.loa.queue.service.domain.message.DocumentArchivingMessage;
import org.apache.activemq.artemis.api.core.ActiveMQBuffer;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentArchivingMessageSerializerTest {

    @Mock
    private ClientSession clientSession;

    @InjectMocks
    private DocumentArchivingMessageSerializer underTest;

    @Test
    void testSerialize() {
        final ClientMessage clientMessage = mock(ClientMessage.class);
        when(clientSession.createMessage(true))
                .thenReturn(clientMessage);
        final ActiveMQBuffer bodyBuffer = mock(ActiveMQBuffer.class);
        when(clientMessage.getBodyBuffer())
                .thenReturn(bodyBuffer);
        final DocumentArchivingMessage documentArchivingMessage = DocumentArchivingMessage.builder()
                .id("id")
                .type("type")
                .source("source")
                .sourceLocationId(Optional.of("sourceLocationId"))
                .contentLength(5)
                .originalContentLength(8)
                .checksum("checksum")
                .compression("compression")
                .build();

        underTest.serialize(documentArchivingMessage);

        final InOrder inOrder = Mockito.inOrder(bodyBuffer);

        inOrder.verify(bodyBuffer).writeString("id");
        inOrder.verify(bodyBuffer).writeString("type");
        inOrder.verify(bodyBuffer).writeString("source");
        inOrder.verify(bodyBuffer).writeBoolean(true);
        inOrder.verify(bodyBuffer).writeString("sourceLocationId");
        inOrder.verify(bodyBuffer).writeLong(5);
        inOrder.verify(bodyBuffer).writeLong(8);
        inOrder.verify(bodyBuffer).writeString("checksum");
        inOrder.verify(bodyBuffer).writeString("compression");
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void testSerializeWhenSourceLocationIdIsNotPresent() {
        final ClientMessage clientMessage = mock(ClientMessage.class);
        when(clientSession.createMessage(true))
                .thenReturn(clientMessage);
        final ActiveMQBuffer bodyBuffer = mock(ActiveMQBuffer.class);
        when(clientMessage.getBodyBuffer())
                .thenReturn(bodyBuffer);
        final DocumentArchivingMessage documentArchivingMessage = DocumentArchivingMessage.builder()
                .id("id")
                .type("type")
                .source("source")
                .sourceLocationId(Optional.empty())
                .contentLength(5)
                .originalContentLength(8)
                .checksum("checksum")
                .compression("compression")
                .build();

        underTest.serialize(documentArchivingMessage);

        final InOrder inOrder = Mockito.inOrder(bodyBuffer);

        inOrder.verify(bodyBuffer).writeString("id");
        inOrder.verify(bodyBuffer).writeString("type");
        inOrder.verify(bodyBuffer).writeString("source");
        inOrder.verify(bodyBuffer).writeBoolean(false);
        inOrder.verify(bodyBuffer).writeLong(5);
        inOrder.verify(bodyBuffer).writeLong(8);
        inOrder.verify(bodyBuffer).writeString("checksum");
        inOrder.verify(bodyBuffer).writeString("compression");
        inOrder.verifyNoMoreInteractions();
    }
}
