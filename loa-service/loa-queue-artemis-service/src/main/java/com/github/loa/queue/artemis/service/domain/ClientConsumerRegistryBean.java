package com.github.loa.queue.artemis.service.domain;

import com.github.loa.queue.service.domain.Queue;
import lombok.Builder;
import lombok.Getter;
import org.apache.activemq.artemis.api.core.client.ClientConsumer;

@Getter
@Builder
public class ClientConsumerRegistryBean {

    private final Queue queue;
    private final ClientConsumer clientConsumer;
}
