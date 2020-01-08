package com.github.loa.vault.service.listener;

import com.github.loa.vault.service.VaultDocumentManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Slf4j
@Service
@RequiredArgsConstructor
public class VaultQueueListener implements CommandLineRunner {

    private final VaultQueueConsumer vaultQueueConsumer;
    private final VaultDocumentManager vaultDocumentManager;

    @Override
    public void run(final String... args) {
        Flux.generate(vaultQueueConsumer)
                .flatMap(vaultDocumentManager::archiveDocument)
                .subscribe();
    }
}
