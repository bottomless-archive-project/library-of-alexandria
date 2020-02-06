package com.github.loa.queue.artemis.service.producer;

import com.github.loa.queue.artemis.configuration.QueueServerConfiguration;
import com.github.loa.queue.artemis.service.producer.pool.domain.PoolableClientProducer;
import com.github.loa.queue.service.domain.Queue;
import com.github.loa.queue.service.domain.QueueException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.artemis.api.core.client.ClientProducer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;
import stormpot.Pool;
import stormpot.Timeout;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnMissingBean(QueueServerConfiguration.class)
public class ClientProducerExecutor {

    private final PoolableClientProducerPoolFactory poolableClientProducerPoolFactory;

    public void invokeProducer(final Queue queue, final Consumer<ClientProducer> clientProducerConsumer) {
        try (final PoolableClientProducer poolableClientProducer = claimClientProducer(queue)) {
            clientProducerConsumer.accept(poolableClientProducer.getClientProducer());
        }
    }

    private PoolableClientProducer claimClientProducer(final Queue queue) {
        final Pool<PoolableClientProducer> clientProducersForQueue = poolableClientProducerPoolFactory.getPool(queue);

        try {
            return clientProducersForQueue.claim(new Timeout(Integer.MAX_VALUE, TimeUnit.DAYS));
        } catch (final InterruptedException e) {
            throw new QueueException("Unable to acquire client producer!", e);
        }
    }
}
