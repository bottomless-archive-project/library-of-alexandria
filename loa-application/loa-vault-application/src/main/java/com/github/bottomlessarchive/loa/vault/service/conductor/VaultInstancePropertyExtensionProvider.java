package com.github.bottomlessarchive.loa.vault.service.conductor;

import com.github.bottomlessarchive.loa.conductor.service.client.extension.InstancePropertyExtensionProvider;
import com.github.bottomlessarchive.loa.conductor.service.client.extension.domain.InstanceExtensionContext;
import com.github.bottomlessarchive.loa.vault.service.VaultDocumentManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VaultInstancePropertyExtensionProvider implements InstancePropertyExtensionProvider {

    private final VaultDocumentManager vaultDocumentManager;

    @Override
    public void extendInstanceWithProperty(InstanceExtensionContext instanceExtensionContext) {
        instanceExtensionContext.setProperty("freeSpace", String.valueOf(vaultDocumentManager.getAvailableSpace()));
    }
}
