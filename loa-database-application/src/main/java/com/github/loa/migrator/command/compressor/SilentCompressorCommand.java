package com.github.loa.migrator.command.compressor;

import com.github.loa.compression.domain.DocumentCompression;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty("initialize-indexer")
public class SilentCompressorCommand implements CommandLineRunner {

    private final DocumentEntityFactory documentEntityFactory;

    @Override
    public void run(String... args) throws Exception {
        final List<DocumentEntity> documentEntities = documentEntityFactory.getDocumentEntity(DocumentCompression.NONE);

        /*documentEntities.stream()
                .forEach(documentEntity -> );*/
    }
}
