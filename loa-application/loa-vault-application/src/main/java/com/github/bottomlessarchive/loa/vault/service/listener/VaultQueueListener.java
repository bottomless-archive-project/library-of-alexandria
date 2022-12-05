package com.github.bottomlessarchive.loa.vault.service.listener;

import com.github.bottomlessarchive.loa.threading.thread.ThreadManipulator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "loa.vault.archiving", havingValue = "true")
public class VaultQueueListener implements CommandLineRunner {

    private final ThreadManipulator threadManipulator;
    private final ArchivingMessageProcessor archivingMessageProcessor;

    @Override
    public void run(final String... args) {
        threadManipulator.runInNewThread(archivingMessageProcessor::processMessages);
    }
}
