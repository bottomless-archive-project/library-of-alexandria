package com.github.loa.vault.service.location.s3;

import com.github.loa.compression.domain.DocumentCompression;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.vault.service.location.VaultLocation;
import com.github.loa.vault.service.location.VaultLocationFactory;
import com.github.loa.vault.service.location.s3.domain.S3VaultLocation;
import org.springframework.stereotype.Service;

@Service
public class S3VaultLocationFactory implements VaultLocationFactory {

    //TODO: We should be able to configure this from a config property!
    private static final String BUCKET_NAME = "document-archive";

    @Override
    public VaultLocation getLocation(final DocumentEntity documentEntity) {
        return getLocation(documentEntity, documentEntity.getCompression());
    }

    @Override
    public VaultLocation getLocation(final DocumentEntity documentEntity, final DocumentCompression compression) {
        final String fileName = compression == DocumentCompression.NONE
                ? documentEntity.getId() + "." + documentEntity.getType().getFileExtension()
                : documentEntity.getId() + "." + documentEntity.getType().getFileExtension() + "."
                + compression.getFileExtension();
        final String contentType = compression == DocumentCompression.NONE
                ? documentEntity.getType().getMimeType()
                : compression.getMimeType();

        return new S3VaultLocation(BUCKET_NAME, fileName, contentType);
    }
}
