package com.github.loa.vault.client.service;

import com.github.loa.compression.domain.DocumentCompression;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.vault.client.configuration.VaultClientConfigurationProperties;
import com.github.loa.vault.client.service.domain.VaultAccessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.net.http.HttpRequest.BodyPublishers.ofString;

@Service
@RequiredArgsConstructor
public class VaultClientService {

    private final VaultClientConfigurationProperties vaultClientConfigurationProperties;

    public void createDocument(final DocumentEntity documentEntity, final InputStream documentContent)
            throws IOException {
        final MultipartRequest multipartRequest = new MultipartRequest("http://"
                + vaultClientConfigurationProperties.getHost() + ":" + vaultClientConfigurationProperties.getPort()
                + "/document/" + documentEntity.getId());

        multipartRequest.addFilePart("content", documentContent);

        int response = multipartRequest.execute();

        if (response != 200) {
            throw new VaultAccessException("Unable to index document " + documentEntity.getId()
                    + "! Returned status code: " + response + ",");
        }
    }

    public InputStream queryDocument(final DocumentEntity documentEntity) {
        try {
            return new URL("http://" + vaultClientConfigurationProperties.getHost() + ":"
                    + vaultClientConfigurationProperties.getPort() + "/document/" + documentEntity.getId()).openStream();
        } catch (IOException e) {
            throw new VaultAccessException("Unable to get content for document " + documentEntity.getId()
                    + " from vault!", e);
        }
    }

    public byte[] queryDocumentRaw(final DocumentEntity documentEntity) {
        try {
            final URL documentLocation = new URL("http://" + vaultClientConfigurationProperties.getHost() + ":"
                    + vaultClientConfigurationProperties.getPort() + "/document/" + documentEntity.getId());

            return documentLocation.openStream().readAllBytes();
        } catch (IOException e) {
            throw new VaultAccessException("Unable to get content for document " + documentEntity.getId()
                    + " from vault!", e);
        }
    }

    public void recompressDocument(final DocumentEntity documentEntity, final DocumentCompression documentCompression) {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://" + vaultClientConfigurationProperties.getHost() + ":"
                        + vaultClientConfigurationProperties.getPort() + "/document/" + documentEntity.getId()
                        + "/recompress"))
                .header("Content-Type", "application/json")
                .POST(ofString("{compression: \"" + documentCompression + "\"}"))
                .build();

        HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(System.out::println)
                .join();
    }
}
