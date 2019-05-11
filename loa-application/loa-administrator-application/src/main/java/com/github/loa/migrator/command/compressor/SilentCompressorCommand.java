package com.github.loa.migrator.command.compressor;

import com.github.loa.compression.configuration.CompressionConfigurationProperties;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty("silent-compressor")
public class SilentCompressorCommand implements CommandLineRunner {

    private final DocumentEntityFactory documentEntityFactory;
    private final CompressionConfigurationProperties compressionConfigurationProperties;
    private final RecompressorService recompressorService;

    @Override
    public void run(final String... args) {
        documentEntityFactory.getDocumentEntities()
                .filter(DocumentEntity::isInVault)
                .filter(this::shouldRecompress)
                .forEach(recompressorService::recompress);
    }

    private boolean shouldRecompress(final DocumentEntity documentEntity) {
        return documentEntity.getCompression() != compressionConfigurationProperties.getAlgorithm();
    }
}
