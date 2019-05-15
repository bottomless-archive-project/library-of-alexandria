package com.github.loa.vault.client.service;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.vault.client.configuration.VaultClientConfigurationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

@Service
@RequiredArgsConstructor
public class VaultClientService {

    private final VaultClientConfigurationProperties vaultClientConfigurationProperties;
    private final HttpClient httpClient;

    public void createDocument(final DocumentEntity documentEntity, final InputStream documentContent)
            throws IOException, InterruptedException {
        final HttpRequest mainRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://" + vaultClientConfigurationProperties.getHost() + ":"
                        + vaultClientConfigurationProperties.getPort() + "/document/" + documentEntity.getId()))
                .POST(BodyPublishers.ofInputStream(() -> documentContent))
                .build();

        httpClient.send(mainRequest, BodyHandlers.discarding());
    }

    public InputStream queryDocument(final DocumentEntity documentEntity) {
        try {
            return new URL("http://" + vaultClientConfigurationProperties.getHost() + ":"
                    + vaultClientConfigurationProperties.getPort() + "/document/" + documentEntity.getId()).openStream();
        } catch (IOException e) {
            throw new RuntimeException("Unable to get content for document " + documentEntity.getId()
                    + " from vault!", e);
        }
    }
}
