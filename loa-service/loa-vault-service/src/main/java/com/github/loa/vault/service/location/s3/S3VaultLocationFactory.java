package com.github.loa.vault.service.location.s3;

import com.github.loa.compression.domain.DocumentCompression;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.vault.service.location.VaultLocation;
import com.github.loa.vault.service.location.VaultLocationFactory;
import org.springframework.stereotype.Service;

@Service
public class S3VaultLocationFactory implements VaultLocationFactory {

    @Override
    public VaultLocation getLocation(final DocumentEntity documentEntity) {
        return null;
    }

    @Override
    public VaultLocation getLocation(final DocumentEntity documentEntity, final DocumentCompression compression) {
        return null;
    }
}
