package com.github.bottomlessarchive.loa.downloader.service.source.queue;

import com.github.bottomlessarchive.loa.queue.service.QueueManipulator;
import com.github.bottomlessarchive.loa.queue.service.domain.Queue;
import com.github.bottomlessarchive.loa.queue.service.domain.message.DocumentLocationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "loa.downloader.source", havingValue = "queue", matchIfMissing = true)
public class DownloadQueueListener implements CommandLineRunner {

    private final QueueManipulator queueManipulator;
    private final QueueMessageHandler queueMessageHandler;

    @Override
    public void run(final String... args) {
        queueManipulator.silentlyInitializeQueues(Queue.DOCUMENT_LOCATION_QUEUE, Queue.DOCUMENT_ARCHIVING_QUEUE);

        log.info("Starting document downloading using 'queue' as a source.");

        while (true) {
            queueManipulator.readMessage(Queue.DOCUMENT_LOCATION_QUEUE, DocumentLocationMessage.class)
                    .ifPresent(queueMessageHandler::handleMessage);
        }
    }
}
