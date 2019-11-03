package com.github.loa.generator.command;

import com.github.loa.document.service.location.DocumentLocationValidator;
import com.github.loa.generator.command.batch.DocumentLocationFactory;
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
    private final UrlEncoder urlEncoder;

    @Override
    public void run(final String... args) {
        log.info("Initializing the document location generating.");

        documentLocationFactory.streamLocations()
                .filter(documentLocationValidator::validDocumentLocation)
                .flatMap(urlEncoder::encode)
                .distinct()
                //.doOnNext(System.out::println)
                .subscribe();
    }
}
