package com.github.loa.queue.artemis.service.domain;

import com.github.loa.queue.service.domain.Queue;
import lombok.Builder;
import lombok.Getter;
import org.apache.activemq.artemis.api.core.client.ClientProducer;

@Getter
@Builder
public class ClientProducerRegistryBean {

    private final Queue queue;
    private final ClientProducer clientProducer;
}
