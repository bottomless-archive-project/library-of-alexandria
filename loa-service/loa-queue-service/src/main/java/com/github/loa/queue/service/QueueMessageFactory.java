package com.github.loa.queue.service;

import com.github.loa.queue.service.domain.message.DocumentLocationMessage;
import com.github.loa.queue.service.domain.message.QueueMessage;

public interface QueueMessageFactory {

    QueueMessage newDocumentLocationQueueMessage(final DocumentLocationMessage documentLocationMessage);
}
