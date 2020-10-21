package com.github.loa.vault.client.service;

import com.github.loa.compression.domain.DocumentCompression;
import com.github.loa.document.service.DocumentManipulator;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.vault.client.configuration.VaultClientConfigurationProperties;
import com.github.loa.vault.client.configuration.VaultClientLocationConfigurationProperties;
import com.github.loa.vault.client.service.request.RecompressRequest;
import com.github.loa.vault.client.service.response.FreeSpaceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class VaultClientService {

    private final VaultClientConfigurationProperties vaultClientConfigurationProperties;
    private final DocumentManipulator documentManipulator;
    private final WebClient vaultWebClient;

    /**
     * Requests and returns the content of a document. If the document is not found or an error happened while doing
     * the request, an empty {@link Mono} is returned.
     *
     * @param documentEntity the document entity to get the content for
     * @return the content of the document
     */
    //TODO: Instead of returning empty, we should return an error when the request is failed!
    public Mono<byte[]> queryDocument(final DocumentEntity documentEntity) {
        final VaultClientLocationConfigurationProperties vaultClientLocationConfigurationProperties =
                vaultClientConfigurationProperties.getLocation(documentEntity.getVault());

        return vaultWebClient.get()
                .uri(vaultClientLocationConfigurationProperties.getLocation() + "/document/" + documentEntity.getId())
                .retrieve()
                .bodyToMono(byte[].class)
                .onErrorResume(WebClientResponseException.class, (error) -> {
                    log.info("Missing document with id: {}!", documentEntity.getId());

                    // TODO: Maybe its a better idea to do this in the vault itself?
                    // This could happen when the vault is closed forcefully and the file is not/partially saved.
                    if (error.getResponseBodyAsString().contains("Unable to get the content of a vault location!")
                            || error.getResponseBodyAsString().contains("Error while decompressing document!")) {
                        return documentManipulator.markIndexFailure(documentEntity.getId())
                                .then(Mono.empty());
                    }

                    return Mono.empty();
                });
    }

    public Mono<Void> recompressDocument(final DocumentEntity documentEntity, final DocumentCompression documentCompression) {
        final VaultClientLocationConfigurationProperties vaultClientLocationConfigurationProperties =
                vaultClientConfigurationProperties.getLocation(documentEntity.getVault());

        return vaultWebClient.post()
                .uri(vaultClientLocationConfigurationProperties.getLocation() + "/document/" + documentEntity.getId() + "/recompress")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(
                        RecompressRequest.builder()
                                .compression(documentCompression)
                                .build()
                )
                .retrieve()
                .toBodilessEntity()
                .then();
    }

    public Mono<Long> getAvailableSpace(final String vaultName) {
        final VaultClientLocationConfigurationProperties vaultClientLocationConfigurationProperties =
                vaultClientConfigurationProperties.getLocation(vaultName);

        return vaultWebClient.get()
                .uri(vaultClientLocationConfigurationProperties.getLocation() + "/free-space")
                .retrieve()
                .bodyToMono(FreeSpaceResponse.class)
                .map(FreeSpaceResponse::getFreeSpace);
    }
}
