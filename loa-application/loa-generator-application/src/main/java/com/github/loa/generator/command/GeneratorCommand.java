package com.github.loa.generator.command;

import com.github.loa.generator.command.batch.DocumentLocationFactory;
import com.github.loa.location.service.DocumentLocationValidator;
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
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeneratorCommand implements CommandLineRunner {

    private final DocumentLocationFactory documentLocationFactory;
    private final DocumentLocationValidator documentLocationValidator;
    private final DocumentSourceItemFactory documentSourceItemFactory;
    private final ClientSessionFactory clientSessionFactory;
    private final UrlEncoder urlEncoder;

    @Override
    public void run(final String... args) throws ActiveMQException {
        final ClientSession clientSession = clientSessionFactory.createSession();
        final ClientProducer clientProducer = clientSession.createProducer("loa-document-location");

        final ClientSession.QueueQuery queueQuery = clientSession
                .queueQuery(SimpleString.toSimpleString("loa-document-location"));

        if (!queueQuery.isExists()) {
            clientSession.createQueue("loa-document-location", RoutingType.ANYCAST,
                    "loa-document-location", true);
        }

        log.info("Initializing the document location generating.");

        documentLocationFactory.streamLocations()
                .filter(documentLocationValidator::validDocumentLocation)
                .flatMap(urlEncoder::encode)
                .map(documentSourceItemFactory::newDocumentSourceItem)
                .doOnNext(documentSourceItem -> {
                    final ClientMessage message = clientSession.createMessage(true);

                    message.getBodyBuffer().writeString(documentSourceItem.getSourceName());
                    message.getBodyBuffer().writeString(documentSourceItem.getDocumentLocation().toString());

                    try {
                        clientProducer.send(message);
                    } catch (ActiveMQException e) {
                        log.error("Unable to send message!");
                    }
                })
                .subscribe();
    }
}
