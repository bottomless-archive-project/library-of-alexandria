package com.github.bottomlessarchive.loa.queue.service;

import com.github.bottomlessarchive.loa.queue.service.domain.Queue;
import com.github.bottomlessarchive.loa.queue.service.domain.QueueException;

import java.util.Optional;

public interface QueueManipulator {

    /**
     * Initialize the queues in the Queue Application if it does not exist. If one of the queues already exist it does nothing for that
     * queue.
     *
     * @param queues the queues to initialize
     * @throws QueueException when an error happens while trying to create the queue
     */
    void silentlyInitializeQueues(Queue... queues);

    /**
     * Initialize the queue in the Queue Application if it does not exist. If the queue already exist it does nothing.
     *
     * @param queue the queue to initialize
     * @throws QueueException when an error happens while trying to create the queue
     */
    void silentlyInitializeQueue(Queue queue);

    /**
     * Initialize a queue in the Queue Application.
     *
     * @param queue the queue to initialize
     * @throws QueueException when an error happens while trying to create the queue
     */
    void initializeQueue(Queue queue);

    /**
     * Returns the message count that is available in the provided {@link Queue}.
     *
     * @param queue the queue to get the message count for
     * @return the message count in the queue
     * @throws QueueException when an error happens while trying to create the queue
     */
    long getMessageCount(Queue queue);

    /**
     * Sends the provided message to the provided queue.
     *
     * @param queue   the queue to send the message to
     * @param message the message to send
     * @throws QueueException when an error happens while trying to send the message
     */
    <T> void sendMessage(Queue queue, T message);

    /**
     * Reads a message from the provided queue.
     *
     * @param queue the queue to read the message from
     * @return the message that's being read
     * @throws QueueException when an error happens while trying to read the message
     */
    <T> Optional<T> readMessage(Queue queue, Class<T> resultType);
}
