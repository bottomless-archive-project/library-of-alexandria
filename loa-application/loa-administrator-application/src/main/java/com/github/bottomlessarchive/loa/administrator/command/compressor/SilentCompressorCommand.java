package com.github.bottomlessarchive.loa.administrator.command.compressor;

import com.github.bottomlessarchive.loa.administrator.command.compressor.configuration.SilentCompressorConfigurationProperties;
import com.github.bottomlessarchive.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.vault.client.service.VaultClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * This command will go through every {@link DocumentEntity} in the database and ask the Vault Application to recompress them when the
 * compression is not the same as provided.
 *
 * <ul>
 *      <li>The desired compression should be set with the {@code --loa.command.silent-compressor.algorithm} command-line argument.</li>
 *      <li>The desired parallelism level should be set with the {@code --loa.command.silent-compressor.parallelism-level} command-line
 *          argument.</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "loa.command", havingValue = "silent-compressor")
public class SilentCompressorCommand implements CommandLineRunner {

    private final DocumentEntityFactory documentEntityFactory;
    private final SilentCompressorConfigurationProperties silentCompressorConfigurationProperties;
    private final VaultClientService vaultClientService;

    /**
     * Runs the command.
     *
     * @param args the command arguments, none yet
     */
    @Override
    public void run(final String... args) {
        if (!silentCompressorConfigurationProperties.hasAlgorithm()) {
            throw new IllegalArgumentException("The loa.command.silent-compressor.algorithm command-line argument must be set!");
        }

        documentEntityFactory.getDocumentEntitiesSync()
                .parallel()
                .filter(DocumentEntity::isArchived)
                .filter(this::shouldCompress)
                .forEach(documentEntity -> vaultClientService.recompressDocument(documentEntity,
                        silentCompressorConfigurationProperties.algorithm()));
    }

    private boolean shouldCompress(final DocumentEntity documentEntity) {
        return documentEntity.getCompression() != silentCompressorConfigurationProperties.algorithm();
    }
}
