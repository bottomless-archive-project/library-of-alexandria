package com.github.loa.vault.client.service;

import com.github.loa.compression.domain.DocumentCompression;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.vault.client.service.request.QueryDocumentRequest;
import com.github.loa.vault.client.service.request.RecompressRequest;
import com.github.loa.vault.client.service.response.FreeSpaceResponse;
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

    /**
     * Requests and returns the content of a document. If the document is not found or an error happened while doing
     * the request, an empty {@link Mono} is returned.
     *
     * @param documentEntity the document entity to get the content for
     * @return the content of the document
     */
    public Mono<byte[]> queryDocument(final DocumentEntity documentEntity) {
        return rSocketRequester.get(documentEntity.getVault())
                .route("queryDocument")
                .data(
                        QueryDocumentRequest.builder()
                                .documentId(documentEntity.getId().toString())
                                .build()
                )
                .retrieveMono(QueryDocumentResponse.class)
                //TODO: In the old implementation there was an error handling here that stated: "This could happen when the vault is closed
                // forcefully and the file is not/partially saved."
                .map(QueryDocumentResponse::getPayload);
    }

    public Mono<Void> recompressDocument(final DocumentEntity documentEntity, final DocumentCompression documentCompression) {
        return rSocketRequester.get(documentEntity.getVault())
                .route("recompressDocument")
                .data(
                        RecompressRequest.builder()
                                .documentId(documentEntity.getId().toString())
                                .compression(documentCompression)
                                .build()
                )
                .retrieveMono(Void.class);
    }

    public Mono<Long> getAvailableSpace(final String vaultName) {
        return rSocketRequester.get(vaultName)
                .route("freeSpace")
                .retrieveMono(FreeSpaceResponse.class)
                .map(FreeSpaceResponse::getFreeSpace);
    }
}
