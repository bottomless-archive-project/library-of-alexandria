package com.github.loa.vault.service.listener;

import com.github.loa.stage.service.StageLocationFactory;
import com.github.loa.vault.service.VaultDocumentManager;
import com.github.loa.vault.service.domain.DocumentArchivingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VaultQueueListener implements CommandLineRunner {

    private final StageLocationFactory stageLocationFactory;
    private final VaultQueueConsumer vaultQueueConsumer;
    private final VaultDocumentManager vaultDocumentManager;

    @Override
    public void run(final String... args) {
        Flux.generate(vaultQueueConsumer)
                .flatMap(archivingContext -> {
                    final String documentId = UUID.randomUUID().toString();

                    log.info("Archiving document with id: {}.", documentId);

                    return stageLocationFactory.getLocation(documentId, archivingContext.getType())
                            .flatMap(documentLocation -> {
                                try (final FileOutputStream fileOutputStream =
                                             new FileOutputStream(documentLocation.getPath().toFile())) {

                                    fileOutputStream.write(((ByteArrayOutputStream)
                                            archivingContext.getContent()).toByteArray());
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }

                                return Mono.just(documentLocation)
                                        .map(fileLocation -> DocumentArchivingContext.builder()
                                                .type(archivingContext.getType())
                                                .location(archivingContext.getLocation().toString())
                                                .source(archivingContext.getSource())
                                                .contents(fileLocation.getPath())
                                                .build()
                                        )
                                        .flatMap(vaultDocumentManager::archiveDocument)
                                        .doOnTerminate(documentLocation::cleanup);
                            });
                })
                .subscribe();
    }
}
