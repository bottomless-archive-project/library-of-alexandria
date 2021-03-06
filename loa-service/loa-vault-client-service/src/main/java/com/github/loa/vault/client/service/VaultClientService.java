package com.github.loa.vault.client.service;

import com.github.loa.compression.domain.DocumentCompression;
import com.github.loa.document.service.DocumentManipulator;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.vault.client.service.request.DeleteDocumentRequest;
import com.github.loa.vault.client.service.request.QueryDocumentRequest;
import com.github.loa.vault.client.service.request.RecompressRequest;
import com.github.loa.vault.client.service.response.FreeSpaceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class VaultClientService {

    private final DocumentManipulator documentManipulator;
    private final Map<String, RSocketRequester> rSocketRequester;

    /**
     * Requests and returns the content of a document. If the document is not found or an error happened while doing
     * the request, an empty {@link Mono} is returned.
     *
     * @param documentEntity the document entity to get the content for
     * @return the content of the document
     */
    public Flux<DataBuffer> queryDocument(final DocumentEntity documentEntity) {
        return rSocketRequester.get(documentEntity.getVault())
                .route("queryDocument")
                .data(
                        QueryDocumentRequest.builder()
                                .documentId(documentEntity.getId().toString())
                                .build()
                )
                .retrieveFlux(DataBuffer.class)
                .onErrorResume(RuntimeException.class, (error) -> {
                    log.info("Missing document with id: {}!", documentEntity.getId());

                    // TODO: Maybe its a better idea to do this in the vault itself?
                    // This could happen when the vault is closed forcefully and the file is not/partially saved.
                    if (error.getMessage().contains("Unable to get document content on a vault location!")
                            || error.getMessage().contains("Error while decompressing document!")) {
                        return documentManipulator.markCorrupt(documentEntity.getId())
                                .then(Mono.empty());
                    }

                    return Mono.empty();
                });
    }

    /**
     * Requests the provided document to be deleted from the vault it resides in.
     *
     * @param documentEntity the document to be deleted
     * @return an empty response
     */
    public Mono<Void> deleteDocument(final DocumentEntity documentEntity) {
        return rSocketRequester.get(documentEntity.getVault())
                .route("deleteDocument")
                .data(
                        DeleteDocumentRequest.builder()
                                .documentId(documentEntity.getId().toString())
                                .build()
                )
                .retrieveMono(Void.class);
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
