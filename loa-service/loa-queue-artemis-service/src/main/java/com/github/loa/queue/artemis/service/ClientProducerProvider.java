package com.github.loa.queue.artemis.service;

import com.github.loa.queue.artemis.configuration.QueueServerConfiguration;
import com.github.loa.queue.artemis.service.domain.ClientProducerRegistryBean;
import com.github.loa.queue.service.domain.Queue;
import org.apache.activemq.artemis.api.core.client.ClientProducer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@ConditionalOnMissingBean(QueueServerConfiguration.class)
public class ClientProducerProvider {

    private final Map<Queue, ClientProducer> clientProducerMap = new EnumMap<>(Queue.class);

    public ClientProducerProvider(final List<ClientProducerRegistryBean> clientProducerRegistryBeanList) {
        clientProducerRegistryBeanList.forEach(clientProducerRegistryBean -> clientProducerMap.put(
                clientProducerRegistryBean.getQueue(), clientProducerRegistryBean.getClientProducer()));
    }

    public ClientProducer getClientProducer(final Queue queue) {
        return clientProducerMap.get(queue);
    }
}
