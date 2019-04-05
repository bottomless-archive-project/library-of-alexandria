package com.github.loa.migrator.command;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "encode-url-migration")
public class UrlEncodeMigrationCommand implements CommandLineRunner {

    @Override
    @Transactional
    public void run(final String... args) {

    }
}
