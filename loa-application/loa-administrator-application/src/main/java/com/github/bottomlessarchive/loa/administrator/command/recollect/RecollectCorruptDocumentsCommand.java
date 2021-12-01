package com.github.bottomlessarchive.loa.administrator.command.recollect;

import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.document.service.entity.factory.DocumentEntityFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty("recollect-corrupt-documents")
public class RecollectCorruptDocumentsCommand implements CommandLineRunner {

    private final DocumentEntityFactory documentEntityFactory;

    @Override
    public void run(final String... args) {
        log.info("Started to run the recollect corrupt documents command!");

        documentEntityFactory.getDocumentEntities()
                .filter(DocumentEntity::isCorrupt)
                .flatMap(this::recollectCorruptDocument)
                .subscribe();
    }

    private Mono<Void> recollectCorruptDocument(final DocumentEntity documentEntity) {
        log.info("Recollecting document entity: {}.", documentEntity);

        //TODO!

        return Mono.empty();
    }
}
