package com.github.bottomlessarchive.loa.vault.client.service;

import com.github.bottomlessarchive.loa.compression.domain.DocumentCompression;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.vault.client.service.domain.VaultAccessException;
import com.github.bottomlessarchive.loa.vault.client.service.domain.VaultLocation;
import com.github.bottomlessarchive.loa.vault.client.service.request.DeleteDocumentRequest;
import com.github.bottomlessarchive.loa.vault.client.service.request.DocumentExistsRequest;
import com.github.bottomlessarchive.loa.vault.client.service.request.RecompressRequest;
import com.github.bottomlessarchive.loa.vault.client.service.request.ReplaceCorruptDocumentRequest;
import com.github.bottomlessarchive.loa.vault.client.service.response.DocumentExistsResponse;
import com.github.bottomlessarchive.loa.vault.client.service.response.FreeSpaceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class VaultClientService {

    private final OkHttpClient okHttpClient;
    private final Map<String, VaultLocation> vaultLocations;

    /**
     * Requests and returns the content of a document. If the document is not found or an error happened while doing
     * the request, an {@link VaultAccessException} will be thrown.
     *
     * @param documentEntity the document entity to get the content for
     * @return the content of the document
     */
    public InputStream queryDocument(final DocumentEntity documentEntity) {
        if (!vaultLocations.containsKey(documentEntity.getVault())) {
            throw new IllegalStateException("Vault " + documentEntity.getVault() + " is not found!");
        }

        final VaultLocation vaultLocation = vaultLocations.get(documentEntity.getVault());

        final Request request = new Request.Builder()
                .url("http://" + vaultLocation.getLocation() + ":" + vaultLocation.getPort() + "/document/" + documentEntity.getId())
                .get()
                .build();

        try {
            return okHttpClient.newCall(request)
                    .execute()
                    .body()
                    .byteStream();
        } catch (final IOException e) {
            throw new VaultAccessException("Error while connecting to the vault for document:  " + documentEntity.getId() + "!", e);
        }
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

    public Mono<Boolean> documentExists(final DocumentEntity documentEntity) {
        return rSocketRequester.get(documentEntity.getVault())
                .route("documentExists")
                .data(
                        DocumentExistsRequest.builder()
                                .documentId(documentEntity.getId().toString())
                                .build()
                )
                .retrieveMono(DocumentExistsResponse.class)
                .map(DocumentExistsResponse::isExists);
    }

    public Mono<Void> replaceCorruptDocument(final DocumentEntity documentEntity, final byte[] content) {
        return rSocketRequester.get(documentEntity.getVault())
                .route("replaceCorruptDocument")
                .data(
                        ReplaceCorruptDocumentRequest.builder()
                                .documentId(documentEntity.getId().toString())
                                .content(content)
                                .build()
                )
                .retrieveMono(Void.class);
    }
}
