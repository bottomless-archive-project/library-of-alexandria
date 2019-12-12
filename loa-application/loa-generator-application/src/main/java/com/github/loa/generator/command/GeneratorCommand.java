package com.github.loa.generator.command;

import com.github.loa.source.service.DocumentLocationFactory;
import com.github.loa.generator.configuration.GeneratorConfiguration;
import com.github.loa.location.service.DocumentLocationValidator;
import com.github.loa.source.domain.DocumentSourceItem;
import com.github.loa.source.service.DocumentSourceItemFactory;
import com.github.loa.url.service.UrlEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.RoutingType;
import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.apache.activemq.artemis.api.core.client.ClientProducer;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeneratorCommand implements CommandLineRunner {

    private final DocumentLocationFactory documentLocationFactory;
    private final DocumentLocationValidator documentLocationValidator;
    private final DocumentSourceItemFactory documentSourceItemFactory;
    private final ClientSession clientSession;
    private final ClientProducer clientProducer;
    private final UrlEncoder urlEncoder;

    @Override
    public void run(final String... args) throws ActiveMQException {
        final ClientSession.QueueQuery queueQuery = clientSession.queueQuery(
                SimpleString.toSimpleString(GeneratorConfiguration.QUEUE_NAME));

        log.info("Initialized queue processing! There are {} messages available in the queue!",
                queueQuery.getMessageCount());

        if (!queueQuery.isExists()) {
            log.info("Creating the queue because it doesn't exists.");

            clientSession.createQueue(GeneratorConfiguration.QUEUE_ADDRESS, RoutingType.ANYCAST,
                    GeneratorConfiguration.QUEUE_NAME, true);
        }

        log.info("Initializing the document location generating.");

        documentLocationFactory.streamLocations()
                .filter(documentLocationValidator::validDocumentLocation)
                .flatMap(urlEncoder::encode)
                .map(documentSourceItemFactory::newDocumentSourceItem)
                .map(this::buildMessage)
                .doOnNext(this::sendMessage)
                .subscribe();
    }

    private ClientMessage buildMessage(final DocumentSourceItem documentSourceItem) {
        final ClientMessage message = clientSession.createMessage(true);

        message.getBodyBuffer().writeString(documentSourceItem.getSourceName());
        message.getBodyBuffer().writeString(documentSourceItem.getDocumentLocation().toString());

        return message;
    }

    private void sendMessage(final ClientMessage message) {
        try {
            clientProducer.send(message);
        } catch (ActiveMQException e) {
            log.error("Unable to send message!", e);
        }
    }
}
