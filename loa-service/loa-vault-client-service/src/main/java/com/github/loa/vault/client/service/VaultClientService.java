package com.github.loa.vault.client.service;

import com.github.loa.compression.domain.DocumentCompression;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.vault.client.configuration.VaultClientConfigurationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static java.net.http.HttpRequest.BodyPublishers.ofString;

@Service
@RequiredArgsConstructor
public class VaultClientService {

    private final VaultClientConfigurationProperties vaultClientConfigurationProperties;
    private final WebClient vaultWebClient;

    public Mono<Void> archiveDocument(final DocumentEntity documentEntity, final File documentStageLocation) {
        return vaultWebClient.post()
                .uri(URI.create("http://" + vaultClientConfigurationProperties.getHost() + ":"
                        + vaultClientConfigurationProperties.getPort() + "/document/" + documentEntity.getId()))
                .header("Content-Type", "application/json")
                .body(BodyInserters.fromResource(new FileSystemResource(documentStageLocation)))
                .exchange()
                .then();
    }

    public Mono<byte[]> queryDocument(final DocumentEntity documentEntity) {
        return vaultWebClient.get()
                .uri("/document/" + documentEntity.getId())
                .retrieve()
                .bodyToMono(byte[].class)
                .timeout(Duration.ofMinutes(5));
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
                .join();
    }

    //TODO: Instead of string return the DocumentEntity as a response.
    public Mono<String> removeDocument(final DocumentEntity documentEntity) {
        return vaultWebClient.delete()
                .uri("/document/" + documentEntity.getId())
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofMinutes(5));
    }
}
