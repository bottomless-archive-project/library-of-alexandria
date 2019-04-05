package com.github.loa.migrator.command;

import com.github.loa.document.service.DocumentManipulator;
import com.github.loa.document.service.domain.DocumentStatus;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.document.service.id.factory.DocumentIdFactory;
import com.github.loa.url.service.UrlEncoder;
import com.github.loa.vault.service.VaultLocationFactory;
import com.github.loa.vault.service.location.domain.VaultLocation;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.net.MalformedURLException;
import java.net.URL;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "encode-url-migration")
public class UrlEncodeMigrationCommand implements CommandLineRunner {

    private final DocumentEntityFactory documentEntityFactory;
    private final DocumentManipulator documentManipulator;
    private final DocumentIdFactory documentIdFactory;
    private final UrlEncoder urlEncoder;
    private final VaultLocationFactory vaultLocationFactory;

    @Override
    @Transactional
    public void run(final String... args) {
        documentEntityFactory.getDocumentEntities()
                .forEach(documentEntity -> {
                    final URL encodedUrl = urlEncoder.encode(documentEntity.getUrl());

                    if (encodedUrl.toString().equals(documentEntity.getUrl().toString())) {
                        return;
                    }

                    final String newId = documentIdFactory.newDocumentId(encodedUrl);

                    documentManipulator.refreshDocument(documentEntity.getId(), newId, encodedUrl.toString());

                    if (documentEntity.isInVault()) {
                        final VaultLocation oldLocation = vaultLocationFactory.getLocation(documentEntity);
                        final VaultLocation newLocation = vaultLocationFactory.getLocation(newId);

                        oldLocation.move(newLocation);
                    }
                });
    }
}
