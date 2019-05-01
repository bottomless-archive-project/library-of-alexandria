package com.github.loa.migrator.command.compressor;

import com.github.loa.compression.configuration.CompressionConfigurationProperties;
import com.github.loa.document.service.DocumentManipulator;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.vault.service.VaultDocumentManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecompressorService {

    private final VaultDocumentManager vaultDocumentManager;
    private final DocumentManipulator documentManipulator;
    private final CompressionConfigurationProperties compressionConfigurationProperties;

    /*
     * Creating a new @Transactional here so things get updated even if the original transactional is not fully
     *  finished because the command is stopped mid-run.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recompress(final DocumentEntity documentEntity) {
        log.info("Migrating archived document " + documentEntity.getId() + " from "
                + documentEntity.getCompression() + " compression to "
                + compressionConfigurationProperties.getAlgorithm() + ".");

        final byte[] documentContent = vaultDocumentManager.readDocument(documentEntity);

        vaultDocumentManager.removeDocument(documentEntity);
        vaultDocumentManager.archiveDocument(documentEntity, new ByteArrayInputStream(documentContent));

        documentManipulator.updateCompression(documentEntity.getId(),
                compressionConfigurationProperties.getAlgorithm());
    }
}
