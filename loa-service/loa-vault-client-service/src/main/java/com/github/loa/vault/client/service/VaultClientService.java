package com.github.loa.vault.client.service;

import com.github.loa.compression.domain.DocumentCompression;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.vault.client.configuration.VaultClientConfigurationProperties;
import com.github.loa.vault.client.configuration.VaultClientLocationConfigurationProperties;
import com.github.loa.vault.client.service.request.QueryDocumentRequest;
import com.github.loa.vault.client.service.response.QueryDocumentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class VaultClientService {

    private final Map<String, RSocketRequester> rSocketRequester;
    private final VaultClientConfigurationProperties vaultClientConfigurationProperties;

    /**
     * Requests and returns the content of a document. If the document is not found or an error happened while doing
     * the request, an empty {@link Mono} is returned.
     *
     * @param documentEntity the document entity to get the content for
     * @return the content of the document
     */
    //TODO: Instead of returning empty, we should return an error when the request is failed!
    public Mono<byte[]> queryDocument(final DocumentEntity documentEntity) {
        return rSocketRequester.get(documentEntity.getVault())
                .route("queryDocument")
                .data(QueryDocumentRequest.builder()
                        .documentId(documentEntity.getId().toString())
                        .build()
                )
                .retrieveMono(QueryDocumentResponse.class)
                //TODO: In the old implementation there was an error handling here that stated: "This could happen when the vault is closed
                // forcefully and the file is not/partially saved."
                .map(QueryDocumentResponse::getPayload);
    }

    public Mono<Void> recompressDocument(final DocumentEntity documentEntity, final DocumentCompression documentCompression) {
        final VaultClientLocationConfigurationProperties vaultClientLocationConfigurationProperties =
                vaultClientConfigurationProperties.getLocation(documentEntity.getVault());

        // TODO
        /*return vaultWebClient.post()
                .uri(vaultClientLocationConfigurationProperties.getLocation() + "/document/" + documentEntity.getId() + "/recompress")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(
                        RecompressRequest.builder()
                                .compression(documentCompression)
                                .build()
                )
                .retrieve()
                .toBodilessEntity()
                .then();*/

        return Mono.empty();
    }

    public Mono<Long> getAvailableSpace(final String vaultName) {
        final VaultClientLocationConfigurationProperties vaultClientLocationConfigurationProperties =
                vaultClientConfigurationProperties.getLocation(vaultName);

        //TODO:
        /*
        return vaultWebClient.get()
                .uri(vaultClientLocationConfigurationProperties.getLocation() + "/free-space")
                .retrieve()
                .bodyToMono(FreeSpaceResponse.class)
                .map(FreeSpaceResponse::getFreeSpace);*/

        return Mono.empty();
    }
}
