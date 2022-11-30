package com.github.bottomlessarchive.loa.generator.command;

import com.github.bottomlessarchive.loa.location.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.queue.service.QueueManipulator;
import com.github.bottomlessarchive.loa.queue.service.domain.Queue;
import com.github.bottomlessarchive.loa.queue.service.domain.message.DocumentLocationMessage;
import com.github.bottomlessarchive.loa.source.source.DocumentLocationSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeneratorCommand implements CommandLineRunner {

    private final DocumentLocationSource documentLocationFactory;
    private final QueueManipulator queueManipulator;

    @Override
    public void run(final String... args) {
        log.info("Initializing the document location generating.");

        queueManipulator.silentlyInitializeQueue(Queue.DOCUMENT_LOCATION_QUEUE);

        documentLocationFactory.streamLocations()
                .map(this::transform)
                .forEach(this::sendMessage);
    }

    private DocumentLocationMessage transform(final DocumentLocation documentLocation) {
        return DocumentLocationMessage.builder()
                .id(documentLocation.getId())
                .type(documentLocation.getType().toString())
                .sourceName(documentLocation.getSourceName())
                .documentLocation(documentLocation.getLocation().toString())
                .build();
    }

    private void sendMessage(final DocumentLocationMessage documentLocationMessage) {
        queueManipulator.sendMessage(Queue.DOCUMENT_LOCATION_QUEUE, documentLocationMessage);
    }
}
