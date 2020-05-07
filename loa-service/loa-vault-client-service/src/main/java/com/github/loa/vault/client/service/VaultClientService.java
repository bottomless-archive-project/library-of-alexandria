package com.github.loa.vault.client.service;

import com.github.loa.compression.domain.DocumentCompression;
import com.github.loa.document.service.DocumentManipulator;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.vault.client.configuration.VaultClientConfigurationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.net.http.HttpRequest.BodyPublishers.ofString;

@Slf4j
@Service
@RequiredArgsConstructor
public class VaultClientService {

    private final VaultClientConfigurationProperties vaultClientConfigurationProperties;
    private final DocumentManipulator documentManipulator;
    private final WebClient vaultWebClient;

    public Mono<byte[]> queryDocument(final DocumentEntity documentEntity) {
        return vaultWebClient.get()
                .uri("/document/" + documentEntity.getId())
                .retrieve()
                //TODO: This could be done better! We need to return the bad responses from the client to the caller.
                //This could happen when the vault is closed forcefully and the file is not saved.
                .onStatus(HttpStatus::is5xxServerError, (response) -> {
                    log.info("Missing document with id: {}!", documentEntity.getId());

                    return response.bodyToMono(String.class)
                            .map(error -> {
                                if (error.contains("Unable to get the content of a vault location!")) {
                                    documentManipulator.markIndexFailure(documentEntity.getId()).subscribe();
                                }

                                return Mono.empty();
                            })
                            .then(Mono.empty());
                })
                .bodyToMono(byte[].class);
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
}
