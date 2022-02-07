package com.github.bottomlessarchive.loa.administrator.command.recollect;

import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.document.service.entity.factory.DocumentEntityFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty("recollect-corrupt-documents")
public class RecollectCorruptDocumentsCommand implements CommandLineRunner {

    private final DocumentEntityFactory documentEntityFactory;
    private final DocumentRecollectorService documentRecollectorService;

    @Override
    public void run(final String... args) {
        log.info("Started to run the recollect corrupt documents command!");

        documentEntityFactory.getDocumentEntitiesSync()
                .filter(DocumentEntity::isCorrupt)
                .forEach(documentRecollectorService::recollectCorruptDocument);
    }
}
