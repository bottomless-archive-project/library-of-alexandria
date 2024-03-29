package com.github.bottomlessarchive.loa.vault.service.location.s3;

import com.github.bottomlessarchive.loa.compression.domain.DocumentCompression;
import com.github.bottomlessarchive.loa.vault.service.location.s3.configuration.S3ConfigurationProperties;
import com.github.bottomlessarchive.loa.vault.service.location.s3.domain.S3VaultLocation;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.vault.service.location.VaultLocation;
import com.github.bottomlessarchive.loa.vault.service.location.VaultLocationFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "loa.vault.location.type", havingValue = "s3")
public class S3VaultLocationFactory implements VaultLocationFactory {

    private final S3Client s3Client;
    private final S3ConfigurationProperties s3ConfigurationProperties;

    @Override
    public VaultLocation getLocation(final DocumentEntity documentEntity, final DocumentCompression compression) {
        final String fileName = compression == DocumentCompression.NONE
                ? documentEntity.getId() + "." + documentEntity.getType().getFileExtension()
                : documentEntity.getId() + "." + documentEntity.getType().getFileExtension() + "."
                + compression.getFileExtension();
        final String contentType = compression == DocumentCompression.NONE
                ? documentEntity.getType().getMimeType()
                : compression.getMimeType();

        return new S3VaultLocation(s3ConfigurationProperties.bucketName(), fileName, contentType, s3Client);
    }

    /**
     * Return the available free space in bytes on the location host.
     *
     * @return the free bytes available
     */
    @Override
    public long getAvailableSpace() {
        return Long.MAX_VALUE;
    }
}
