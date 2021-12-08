package com.github.bottomlessarchive.loa.queue.artemis.service;

import com.github.bottomlessarchive.loa.queue.artemis.configuration.QueueServerConfiguration;
import com.github.bottomlessarchive.loa.queue.artemis.service.consumer.ClientConsumerExecutor;
import com.github.bottomlessarchive.loa.queue.artemis.service.consumer.deserializer.MessageDeserializerProvider;
import com.github.bottomlessarchive.loa.queue.artemis.service.producer.ClientProducerExecutor;
import com.github.bottomlessarchive.loa.queue.artemis.service.producer.serializer.MessageSerializer;
import com.github.bottomlessarchive.loa.queue.artemis.service.producer.serializer.MessageSerializerProvider;
import com.github.bottomlessarchive.loa.queue.service.QueueManipulator;
import com.github.bottomlessarchive.loa.queue.service.domain.Queue;
import com.github.bottomlessarchive.loa.queue.service.domain.QueueException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.ActiveMQLargeMessageInterruptedException;
import org.apache.activemq.artemis.api.core.QueueConfiguration;
import org.apache.activemq.artemis.api.core.RoutingType;
import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

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
    private final ClientConsumerExecutor clientConsumerExecutor;
    private final ClientProducerExecutor clientProducerExecutor;
    private final MessageSerializerProvider messageSerializerProvider;
    private final MessageDeserializerProvider messageDeserializerProvider;

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
                    SimpleString.toSimpleString(queue.getName()));

            if (!queueQuery.isExists()) {
                initializeQueue(queue);
            }
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
                    new QueueConfiguration(queue.getName())
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
                    SimpleString.toSimpleString(queue.getName()));

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

        clientProducerExecutor.invokeProducer(queue, clientProducer -> {
            try {
                clientProducer.send(clientMessage);
            } catch (final ActiveMQException e) {
                throw new QueueException("Unable to send message to the " + queue.getName() + " queue!", e);
            }
        });
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
    public <T> T readMessage(final Queue queue, final Class<T> resultType) {
        return clientConsumerExecutor.invokeConsumer(queue, clientConsumer -> {
            try {
                final ClientMessage clientMessage = clientConsumer.receive();

                // The deserialization should be done inside the invocation because the message is only readable while the consumer is
                // locked (and doesn't read a new message for another thread).
                return (T) messageDeserializerProvider.getDeserializer(queue)
                        .orElseThrow(() -> new QueueException("No deserializer found for queue: " + queue.getName() + "!"))
                        .deserialize(clientMessage);
            } catch (final ActiveMQException e) {
                throw new QueueException("Unable to read message from the " + queue.getName() + " queue!", e);
            } catch (final RuntimeException e) {
                // Artemis sometimes throws a RuntimeException that wraps a named exception, even after a named exception happened
                // inside the artemis client. See: https://issues.apache.org/jira/browse/ARTEMIS-3588
                if (e.getCause() instanceof ActiveMQLargeMessageInterruptedException) {
                    throw new QueueException("Unrecoverable error happened when reading a large message.", e);
                } else {
                    throw e;
                }
            }
        });
    }
}
