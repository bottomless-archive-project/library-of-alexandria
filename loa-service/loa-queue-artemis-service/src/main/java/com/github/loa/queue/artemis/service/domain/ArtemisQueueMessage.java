package com.github.loa.queue.artemis.service.domain;

import com.github.loa.queue.service.domain.message.QueueMessage;
import lombok.Builder;
import lombok.Getter;
import org.apache.activemq.artemis.api.core.client.ClientMessage;

@Getter
@Builder
public class ArtemisQueueMessage implements QueueMessage {

    private final ClientMessage clientMessage;
}
