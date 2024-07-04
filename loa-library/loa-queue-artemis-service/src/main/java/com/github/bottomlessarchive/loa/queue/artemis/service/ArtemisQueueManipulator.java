package com.github.bottomlessarchive.loa.queue.artemis.service;

import com.github.bottomlessarchive.loa.queue.artemis.configuration.QueueServerConfiguration;
import com.github.bottomlessarchive.loa.queue.artemis.service.consumer.deserializer.MessageDeserializerProvider;
import com.github.bottomlessarchive.loa.queue.artemis.service.consumer.pool.QueueConsumerProvider;
import com.github.bottomlessarchive.loa.queue.artemis.service.producer.pool.QueueProducerProvider;
import com.github.bottomlessarchive.loa.queue.artemis.service.producer.serializer.MessageSerializer;
import com.github.bottomlessarchive.loa.queue.artemis.service.producer.serializer.MessageSerializerProvider;
import com.github.bottomlessarchive.loa.queue.service.QueueManipulator;
import com.github.bottomlessarchive.loa.queue.service.domain.Queue;
import com.github.bottomlessarchive.loa.queue.service.domain.QueueException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.QueueConfiguration;
import org.apache.activemq.artemis.api.core.RoutingType;
import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * A {@link QueueManipulator} implementation for Apache Artemis.
 *
 * @see <a href="https://activemq.apache.org/components/artemis/">https://activemq.apache.org/components/artemis/</a>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnMissingBean(QueueServerConfiguration.class)
public class ArtemisQueueManipulator implements QueueManipulator {

    private final ClientSessionFactory clientSessionFactory;
    private final QueueConsumerProvider queueConsumerProvider;
    private final QueueProducerProvider queueProducerProvider;
    private final MessageSerializerProvider messageSerializerProvider;
    private final MessageDeserializerProvider messageDeserializerProvider;

    /**
     * Initialize the queues in the Queue Application if it does not exist. If one of the queues already exist it does nothing for that
     * queue.
     *
     * @param queues the queues to initialize
     * @throws QueueException when an error happens while trying to create the queue
     */
    @Override
    public void silentlyInitializeQueues(final Queue... queues) {
        for (final Queue queue : queues) {
            silentlyInitializeQueue(queue);
        }
    }

    /**
     * Initialize the queue in the Queue Application if it doesn't exist. If the queue already exist it does nothing.
     *
     * @param queue the queue to initialize
     * @throws QueueException when an error happens while trying to create the queue
     */
    @Override
    public void silentlyInitializeQueue(final Queue queue) {
        try (ClientSession clientSession = clientSessionFactory.createSession()) {
            final ClientSession.QueueQuery queueQuery = clientSession.queueQuery(
                    SimpleString.of(queue.getName()));

            if (!queueQuery.isExists()) {
                initializeQueue(queue);
            }

            log.info("Initialized queue processing! There are {} messages available in the {} queue!", getMessageCount(queue),
                    queue.getName());
        } catch (final ActiveMQException e) {
            throw new QueueException("Unable to initialize the " + queue.getName() + " queue!", e);
        }
    }

    /**
     * Initialize a queue in the Queue Application.
     *
     * @param queue the queue to initialize
     * @throws QueueException when an error happens while trying to create the queue
     */
    @Override
    public void initializeQueue(final Queue queue) {
        if (log.isInfoEnabled()) {
            log.info("Creating the {} queue because it doesn't exists.", queue.getName());
        }

        try (ClientSession clientSession = clientSessionFactory.createSession()) {
            clientSession.createQueue(
                    QueueConfiguration.of(queue.getName())
                            .setAddress(queue.getAddress())
                            .setRoutingType(RoutingType.ANYCAST)
                            .setDurable(true)
            );
        } catch (final ActiveMQException e) {
            throw new QueueException("Unable to initialize the " + queue.getName() + " queue!", e);
        }
    }

    /**
     * Returns the message count that is available in the provided {@link Queue}.
     *
     * @param queue the queue to get the message count for
     * @return the message count in the queue
     * @throws QueueException when an error happens while trying to create the queue
     */
    @Override
    public long getMessageCount(final Queue queue) {
        try (ClientSession clientSession = clientSessionFactory.createSession()) {
            final ClientSession.QueueQuery queueQuery = clientSession.queueQuery(
                    SimpleString.of(queue.getName()));

            return queueQuery.getMessageCount();
        } catch (final ActiveMQException e) {
            throw new QueueException("Unable to query the " + queue.getName() + " queue!", e);
        }
    }

    /**
     * Sends the provided message to the provided queue.
     *
     * @param queue   the queue to send the message to
     * @param message the message to send
     * @throws QueueException when an error happens while trying to send the message
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> void sendMessage(final Queue queue, final T message) {
        final MessageSerializer<T> messageSerializer = (MessageSerializer<T>) messageSerializerProvider.getSerializer(queue)
                .orElseThrow(() -> new QueueException("No serializer found for queue: " + queue.getName() + "!"));

        final ClientMessage clientMessage = messageSerializer.serialize(message);

        try {
            synchronized (this) {
                queueProducerProvider.getProducer(queue)
                        .getClientProducer()
                        .send(clientMessage);
            }
        } catch (final ActiveMQException e) {
            throw new QueueException("Unable to send message to the " + queue.getName() + " queue!", e);
        }
    }

    /**
     * Reads a message from the provided queue and cast it to the provided type.
     *
     * @param queue      the queue to read the message from
     * @param resultType the type of the message
     * @return the message that's being read
     * @throws QueueException when an error happens while trying to read the message
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> readMessage(final Queue queue, final Class<T> resultType) {
        try {
            final ClientMessage clientMessage = queueConsumerProvider.getConsumer(queue)
                    .getClientConsumer()
                    .receive(100);

            if (clientMessage == null) {
                return Optional.empty();
            }

            final T deserializedClientMessage = (T) messageDeserializerProvider.getDeserializer(queue)
                    .orElseThrow(() -> new QueueException("No deserializer found for queue: " + queue.getName() + "!"))
                    .deserialize(clientMessage);

            return Optional.of(deserializedClientMessage);
        } catch (final ActiveMQException e) {
            throw new QueueException("Unable to read message from the " + queue.getName() + " queue!", e);
        }
    }
}
