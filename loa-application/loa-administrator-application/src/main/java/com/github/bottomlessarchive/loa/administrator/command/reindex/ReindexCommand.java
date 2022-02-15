package com.github.bottomlessarchive.loa.administrator.command.reindex;

import com.github.bottomlessarchive.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * This command will update the status of the {@link DocumentEntity}s available in the database to {@link DocumentStatus#DOWNLOADED}.
 * This way when the Indexer Application is started it will reindex the documents.
 */
@Slf4j
@Component
@ConditionalOnProperty("reindex")
@RequiredArgsConstructor
public class ReindexCommand implements CommandLineRunner {

    private final DocumentEntityFactory documentEntityFactory;

    /**
     * Runs the command.
     *
     * @param args the command arguments, none yet
     */
    @Override
    public void run(final String... args) {
        log.info("Running the reindex command!");

        documentEntityFactory.updateStatus(DocumentStatus.DOWNLOADED);

        log.info("Running the reindex command finished!");
    }
}
