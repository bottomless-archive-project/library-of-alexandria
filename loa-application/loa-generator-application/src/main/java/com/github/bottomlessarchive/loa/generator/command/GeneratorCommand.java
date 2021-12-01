package com.github.bottomlessarchive.loa.generator.command;

import com.github.bottomlessarchive.loa.location.service.DocumentLocationValidator;
import com.github.bottomlessarchive.loa.location.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.queue.service.QueueManipulator;
import com.github.bottomlessarchive.loa.queue.service.domain.Queue;
import com.github.bottomlessarchive.loa.queue.service.domain.message.DocumentLocationMessage;
import com.github.bottomlessarchive.loa.source.source.DocumentLocationSource;
import com.github.bottomlessarchive.loa.url.service.encoder.UrlEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeneratorCommand implements CommandLineRunner {

    private final DocumentLocationSource documentLocationFactory;
    private final DocumentLocationValidator documentLocationValidator;
    private final UrlEncoder urlEncoder;
    private final QueueManipulator queueManipulator;

    @Override
    public void run(final String... args) {
        log.info("Initializing the document location generating.");

        queueManipulator.silentlyInitializeQueue(Queue.DOCUMENT_LOCATION_QUEUE);

        if (log.isInfoEnabled()) {
            log.info("There are {} messages already available in the queue!",
                    queueManipulator.getMessageCount(Queue.DOCUMENT_LOCATION_QUEUE));
        }

        documentLocationFactory.streamLocations()
                .filterWhen(this::validate)
                .flatMap(this::transform)
                .doOnNext(this::sendMessage)
                .subscribe();
    }

    private Mono<Boolean> validate(final DocumentLocation documentLocation) {
        return Mono.just(documentLocationValidator.validDocumentLocation(documentLocation));
    }

    private Mono<DocumentLocationMessage> transform(final DocumentLocation documentLocation) {
        return Mono.justOrEmpty(documentLocation.getLocation().toUrl())
                .flatMap(urlEncoder::encode)
                .map(url -> DocumentLocationMessage.builder()
                        .sourceName(documentLocation.getSourceName())
                        .documentLocation(url.toString())
                        .build()
                );
    }

    private void sendMessage(final DocumentLocationMessage documentLocationMessage) {
        queueManipulator.sendMessage(Queue.DOCUMENT_LOCATION_QUEUE, documentLocationMessage);
    }
}