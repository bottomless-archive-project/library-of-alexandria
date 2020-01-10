package com.github.loa.queue.artemis.service.pool;

import com.github.loa.queue.artemis.service.pool.domain.PoolableClientProducer;
import com.github.loa.queue.service.domain.Queue;
import com.github.loa.queue.service.domain.QueueException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.client.ClientProducer;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import stormpot.Allocator;
import stormpot.Slot;

@Slf4j
@RequiredArgsConstructor
public class ClientProducerAllocator implements Allocator<PoolableClientProducer> {

    private final Queue supportedQueue;
    private final ClientSessionFactory clientSessionFactory;

    @Override
    public PoolableClientProducer allocate(final Slot slot) {
        return new PoolableClientProducer(slot, createProducer());
    }

    @Override
    public void deallocate(final PoolableClientProducer poolableClientProducer) {
        try {
            poolableClientProducer.getClientProducer().close();
        } catch (final ActiveMQException e) {
            throw new QueueException("Unable to close client producer!", e);
        }
    }

    private ClientProducer createProducer() {
        log.info("Creating new producer for queue: " + supportedQueue + "!");

        try {
            final ClientSession clientSession = clientSessionFactory.createSession();

            return clientSession.createProducer(supportedQueue.getAddress());
        } catch (ActiveMQException e) {
            throw new QueueException("Unable to create client producer for queue: " + supportedQueue + "!");
        }
    }
}
