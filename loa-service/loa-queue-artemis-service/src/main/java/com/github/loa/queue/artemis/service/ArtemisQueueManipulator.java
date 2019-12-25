package com.github.loa.queue.artemis.service;

import com.github.loa.queue.artemis.configuration.QueueServerConfiguration;
import com.github.loa.queue.artemis.service.deserializer.MessageDeserializerProvider;
import com.github.loa.queue.artemis.service.domain.ArtemisQueueMessage;
import com.github.loa.queue.service.domain.Queue;
import com.github.loa.queue.service.QueueManipulator;
import com.github.loa.queue.service.domain.QueueException;
import com.github.loa.queue.service.domain.message.QueueMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.RoutingType;
import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.api.core.client.ClientConsumer;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.apache.activemq.artemis.api.core.client.ClientSession;
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

    private final ClientSession clientSession;
    private final ClientConsumer clientConsumer;
    private final ClientProducerProvider clientProducerProvider;
    private final MessageDeserializerProvider messageDeserializerProvider;

    /**
     * Initialize the queue in the Queue Application if it does not exists. If the queue already exist it does nothing.
     *
     * @param queue the queue to initialize
     * @throws QueueException when an error happens while trying to create the queue
     */
    @Override
    public void silentlyInitializeQueue(final Queue queue) {
        try {
            final ClientSession.QueueQuery queueQuery = clientSession.queueQuery(
                    SimpleString.toSimpleString(queue.getName()));

            if (!queueQuery.isExists()) {
                initializeQueue(queue);
            }
        } catch (ActiveMQException e) {
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
        log.info("Creating the {} queue because it doesn't exists.", queue.getName());

        try {
            clientSession.createQueue(queue.getAddress(), RoutingType.ANYCAST, queue.getName(), true);
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
        try {
            final ClientSession.QueueQuery queueQuery = clientSession.queueQuery(
                    SimpleString.toSimpleString(queue.getName()));

            return queueQuery.getMessageCount();
        } catch (ActiveMQException e) {
            throw new QueueException("Unable to query the " + queue.getName() + " queue!", e);
        }
    }

    /**
     * Sends the provided message to the provided queue.
     *
     * @param queue        the queue to send the message to
     * @param queueMessage the message to send
     * @throws QueueException when an error happens while trying to send the message
     */
    //TODO: This should simply send the message without converting it in an upper layer! The conversion should be here!
    @Override
    public void sendMessage(final Queue queue, final QueueMessage queueMessage) {
        try {
            clientProducerProvider.getClientProducer(queue)
                    .send(((ArtemisQueueMessage) queueMessage).getClientMessage());
        } catch (ActiveMQException e) {
            throw new QueueException("Unable to send message to the " + queue.getName() + " queue!", e);
        }
    }

    /**
     * Reads a message from the provided queue.
     *
     * @param queue the queue to read the message from
     * @return the message that's being read
     * @throws QueueException when an error happens while trying to read the message
     */
    @Override
    public Object readMessage(final Queue queue) {
        try {
            final ClientMessage clientMessage = clientConsumer.receive();

            return messageDeserializerProvider.getDeserializer(queue)
                    .orElseThrow(() -> new QueueException("No deserializer found for queue: " + queue.getName() + "!"))
                    .deserialize(clientMessage);
        } catch (ActiveMQException e) {
            throw new QueueException("Unable to read message from the " + queue.getName() + " queue!", e);
        }
    }
}
