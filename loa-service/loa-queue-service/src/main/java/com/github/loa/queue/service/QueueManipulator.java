package com.github.loa.queue.service;

import com.github.loa.queue.service.domain.Queue;
import com.github.loa.queue.service.domain.QueueException;

import java.io.InputStream;
import java.io.OutputStream;

public interface QueueManipulator {

    /**
     * Initialize the queue in the Queue Application if it does not exists. If the queue already exist it does nothing.
     *
     * @param queue the queue to initialize
     * @throws QueueException when an error happens while trying to create the queue
     */
    void silentlyInitializeQueue(final Queue queue);

    /**
     * Initialize a queue in the Queue Application.
     *
     * @param queue the queue to initialize
     * @throws QueueException when an error happens while trying to create the queue
     */
    void initializeQueue(final Queue queue);

    /**
     * Returns the message count that is available in the provided {@link Queue}.
     *
     * @param queue the queue to get the message count for
     * @return the message count in the queue
     * @throws QueueException when an error happens while trying to create the queue
     */
    long getMessageCount(final Queue queue);

    /**
     * Sends the provided message to the provided queue.
     *
     * @param queue   the queue to send the message to
     * @param message the message to send
     * @throws QueueException when an error happens while trying to send the message
     */
    void sendMessage(final Queue queue, final Object message);

    /**
     * Reads a message from the provided queue.
     *
     * @param queue the queue to read the message from
     * @return the message that's being read
     * @throws QueueException when an error happens while trying to read the message
     */
    Object readMessage(final Queue queue);
}
