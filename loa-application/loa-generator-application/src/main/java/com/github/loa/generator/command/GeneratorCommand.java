package com.github.loa.generator.command;

import com.github.loa.location.service.DocumentLocationValidator;
import com.github.loa.queue.service.QueueManipulator;
import com.github.loa.queue.service.domain.Queue;
import com.github.loa.queue.service.domain.message.DocumentLocationMessage;
import com.github.loa.source.service.DocumentLocationFactory;
import com.github.loa.source.service.DocumentSourceItemFactory;
import com.github.loa.url.service.UrlEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeneratorCommand implements CommandLineRunner {

    private final DocumentLocationFactory documentLocationFactory;
    private final DocumentLocationValidator documentLocationValidator;
    private final DocumentSourceItemFactory documentSourceItemFactory;
    private final UrlEncoder urlEncoder;
    private final QueueManipulator queueManipulator;

    @Override
    public void run(final String... args) {
        log.info("Initializing the document location generating.");
        log.info("There are {} messages already available in the queue!",
                queueManipulator.getMessageCount(Queue.DOCUMENT_LOCATION_QUEUE));

        queueManipulator.silentlyInitializeQueue(Queue.DOCUMENT_LOCATION_QUEUE);

        documentLocationFactory.streamLocations()
                .filter(documentLocationValidator::validDocumentLocation)
                .flatMap(urlEncoder::encode)
                //TODO: We don't need to double convert!
                .map(documentSourceItemFactory::newDocumentSourceItem)
                .map(documentSourceItem -> DocumentLocationMessage.builder()
                        .sourceName(documentSourceItem.getSourceName())
                        .documentLocation(documentSourceItem.getDocumentLocation().toString())
                        .build()
                )
                .doOnNext(this::sendMessage)
                .subscribe();
    }

    private void sendMessage(final DocumentLocationMessage documentLocationMessage) {
        queueManipulator.sendMessage(Queue.DOCUMENT_LOCATION_QUEUE, documentLocationMessage);
    }
}
