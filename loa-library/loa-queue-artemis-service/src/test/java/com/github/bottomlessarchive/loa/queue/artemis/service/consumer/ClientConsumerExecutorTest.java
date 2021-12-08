package com.github.bottomlessarchive.loa.queue.artemis.service.consumer;

import com.github.bottomlessarchive.loa.queue.artemis.service.consumer.pool.domain.PoolableQueueConsumer;
import com.github.bottomlessarchive.loa.queue.artemis.service.consumer.pool.domain.QueueConsumer;
import com.github.bottomlessarchive.loa.queue.service.domain.Queue;
import com.github.bottomlessarchive.loa.queue.service.domain.QueueException;
import org.apache.activemq.artemis.api.core.client.ClientConsumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import stormpot.Pool;
import stormpot.Timeout;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientConsumerExecutorTest {

    @Mock
    private PoolableClientConsumerPoolFactory poolableClientConsumerPoolFactory;

    @Captor
    private ArgumentCaptor<Timeout> timeoutArgumentCaptor;

    @InjectMocks
    private ClientConsumerExecutor clientConsumerExecutor;

    @Test
    @SuppressWarnings("unchecked")
    void testInvokeConsumerApplyCorrectly() throws InterruptedException {
        final Pool<PoolableQueueConsumer> poolableQueueConsumerPool = mock(Pool.class);
        when(poolableClientConsumerPoolFactory.getPool(Queue.DOCUMENT_ARCHIVING_QUEUE))
                .thenReturn(poolableQueueConsumerPool);
        final PoolableQueueConsumer poolableQueueConsumer = mock(PoolableQueueConsumer.class);
        when(poolableQueueConsumerPool.claim(timeoutArgumentCaptor.capture()))
                .thenReturn(poolableQueueConsumer);
        final QueueConsumer queueConsumer = mock(QueueConsumer.class);
        when(poolableQueueConsumer.getQueueConsumer())
                .thenReturn(queueConsumer);
        final ClientConsumer clientConsumer = mock(ClientConsumer.class);
        when(queueConsumer.getClientConsumer())
                .thenReturn(clientConsumer);

        final String result = clientConsumerExecutor.invokeConsumer(Queue.DOCUMENT_ARCHIVING_QUEUE, consumer -> {
            assertThat(consumer)
                    .isEqualTo(clientConsumer);

            return "test-result-message";
        });

        assertThat(timeoutArgumentCaptor.getValue().getTimeout())
                .isEqualTo(Integer.MAX_VALUE);
        assertThat(timeoutArgumentCaptor.getValue().getUnit())
                .isEqualTo(TimeUnit.DAYS);
        assertThat(result)
                .isEqualTo("test-result-message");
    }

    @Test
    @SuppressWarnings({"ResultOfMethodCallIgnored", "unchecked"})
    void testReleaseTheConsumerWhenErrorHappens() throws InterruptedException {
        final Pool<PoolableQueueConsumer> poolableQueueConsumerPool = mock(Pool.class);
        when(poolableClientConsumerPoolFactory.getPool(Queue.DOCUMENT_ARCHIVING_QUEUE))
                .thenReturn(poolableQueueConsumerPool);
        final PoolableQueueConsumer poolableQueueConsumer = mock(PoolableQueueConsumer.class);
        when(poolableQueueConsumerPool.claim(any(Timeout.class)))
                .thenReturn(poolableQueueConsumer);
        final QueueConsumer queueConsumer = mock(QueueConsumer.class);
        when(poolableQueueConsumer.getQueueConsumer())
                .thenReturn(queueConsumer);
        final ClientConsumer clientConsumer = mock(ClientConsumer.class);
        when(queueConsumer.getClientConsumer())
                .thenReturn(clientConsumer);

        AtomicBoolean firstInvocation = new AtomicBoolean(true);
        final String result = clientConsumerExecutor.invokeConsumer(Queue.DOCUMENT_ARCHIVING_QUEUE, consumer -> {
            if (firstInvocation.get()) {
                firstInvocation.set(false);

                throw new QueueException("Test exception!");
            } else {
                return "test-result-message";
            }
        });

        assertThat(result)
                .isEqualTo("test-result-message");

        InOrder poolableQueueConsumerCallOrder = Mockito.inOrder(poolableQueueConsumer);

        poolableQueueConsumerCallOrder.verify(poolableQueueConsumer).getQueueConsumer();
        poolableQueueConsumerCallOrder.verify(poolableQueueConsumer).expire();
        poolableQueueConsumerCallOrder.verify(poolableQueueConsumer).getQueueConsumer();
    }
}
