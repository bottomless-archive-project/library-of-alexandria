package com.github.loa.vault.client.service;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.vault.client.configuration.VaultClientConfigurationProperties;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URL;

@Service
@RequiredArgsConstructor
public class VaultClientService {

    private final VaultClientConfigurationProperties vaultClientConfigurationProperties;

    public InputStream getVaultContent(final DocumentEntity documentEntity) {
        try {
            return new URL("http://" + vaultClientConfigurationProperties.getHost() + ":"
                    + vaultClientConfigurationProperties.getPort() + "/document/" + documentEntity.getId()).openStream();
        } catch (IOException e) {
            throw new RuntimeException("Unable to get content for document " + documentEntity.getId()
                    + " from vault!", e);
        }
    }
}
