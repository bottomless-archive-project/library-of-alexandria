package com.github.loa.queue.artemis.service;

import com.github.loa.queue.artemis.configuration.QueueServerConfiguration;
import com.github.loa.queue.artemis.service.domain.ClientConsumerRegistryBean;
import com.github.loa.queue.service.domain.Queue;
import org.apache.activemq.artemis.api.core.client.ClientConsumer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@ConditionalOnMissingBean(QueueServerConfiguration.class)
public class ClientConsumerProvider {

    private final Map<Queue, ClientConsumer> clientConsumerMap = new EnumMap<>(Queue.class);

    public ClientConsumerProvider(final List<ClientConsumerRegistryBean> clientConsumerRegistryBeans) {
        clientConsumerRegistryBeans.forEach(clientProducerRegistryBean -> clientConsumerMap.put(
                clientProducerRegistryBean.getQueue(), clientProducerRegistryBean.getClientConsumer()));
    }

    public ClientConsumer getClientConsumer(final Queue queue) {
        return clientConsumerMap.get(queue);
    }
}
