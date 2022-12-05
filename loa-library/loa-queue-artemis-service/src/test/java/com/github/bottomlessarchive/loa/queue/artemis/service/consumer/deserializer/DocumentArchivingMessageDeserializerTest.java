package com.github.bottomlessarchive.loa.queue.artemis.service.consumer.deserializer;

import com.github.bottomlessarchive.loa.queue.service.domain.message.DocumentArchivingMessage;
import org.apache.activemq.artemis.api.core.ActiveMQBuffer;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentArchivingMessageDeserializerTest {

    @InjectMocks
    private DocumentArchivingMessageDeserializer underTest;

    @Test
    void testDeserialize() {
        final ClientMessage clientMessage = mock(ClientMessage.class);
        final ActiveMQBuffer activeMQBuffer = mock(ActiveMQBuffer.class);
        when(clientMessage.getBodyBuffer())
                .thenReturn(activeMQBuffer);
        when(activeMQBuffer.readString())
                .thenReturn("id", "type", "source", "sourceLocationId");
        when(activeMQBuffer.readBoolean())
                .thenReturn(true);
        when(activeMQBuffer.readLong())
                .thenReturn(5L);

        final DocumentArchivingMessage result = underTest.deserialize(clientMessage);

        assertThat(result.id())
                .isEqualTo("id");
        assertThat(result.type())
                .isEqualTo("type");
        assertThat(result.source())
                .isEqualTo("source");
        assertThat(result.sourceLocationId())
                .isPresent()
                .hasValue("sourceLocationId");
        assertThat(result.contentLength())
                .isEqualTo(5);
    }

    @Test
    void testDeserializeWhenSourceLocationIdIsNotPresent() {
        final ClientMessage clientMessage = mock(ClientMessage.class);
        final ActiveMQBuffer activeMQBuffer = mock(ActiveMQBuffer.class);
        when(clientMessage.getBodyBuffer())
                .thenReturn(activeMQBuffer);
        when(activeMQBuffer.readString())
                .thenReturn("id", "type", "source");
        when(activeMQBuffer.readBoolean())
                .thenReturn(false);
        when(activeMQBuffer.readLong())
                .thenReturn(5L);

        final DocumentArchivingMessage result = underTest.deserialize(clientMessage);

        assertThat(result.id())
                .isEqualTo("id");
        assertThat(result.type())
                .isEqualTo("type");
        assertThat(result.source())
                .isEqualTo("source");
        assertThat(result.sourceLocationId())
                .isEmpty();
        assertThat(result.contentLength())
                .isEqualTo(5);
    }
}
