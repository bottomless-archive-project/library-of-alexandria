package com.github.loa.generator.command;

import com.github.loa.document.service.location.DocumentLocationValidator;
import com.github.loa.generator.command.batch.DocumentLocationFactory;
import com.github.loa.source.service.DocumentSourceItemFactory;
import com.github.loa.url.service.UrlEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeneratorCommand implements CommandLineRunner {

    private final DocumentLocationFactory documentLocationFactory;
    private final DocumentLocationValidator documentLocationValidator;
    private final DocumentSourceItemFactory documentSourceItemFactory;
    private final UrlEncoder urlEncoder;
    private final JmsTemplate jmsTemplate;

    @Override
    public void run(final String... args) {
        log.info("Initializing the document location generating.");

        documentLocationFactory.streamLocations()
                .filter(documentLocationValidator::validDocumentLocation)
                .flatMap(urlEncoder::encode)
                .map(documentSourceItemFactory::newDocumentSourceItem)
                .doOnNext(documentSourceItem ->
                        jmsTemplate.convertAndSend("loa.downloader", documentSourceItem))
                .subscribe();
    }
}
