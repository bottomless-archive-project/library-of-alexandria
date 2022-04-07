package com.github.bottomlessarchive.loa.vault.client.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bottomlessarchive.loa.compression.domain.DocumentCompression;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.vault.client.service.domain.VaultAccessException;
import com.github.bottomlessarchive.loa.vault.client.service.domain.VaultLocation;
import com.github.bottomlessarchive.loa.vault.client.service.request.RecompressDocumentRequest;
import com.github.bottomlessarchive.loa.vault.client.service.request.ReplaceCorruptDocumentRequest;
import com.github.bottomlessarchive.loa.vault.client.service.response.DocumentExistsResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class VaultClientService {

    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient okHttpClient;
    private final ObjectMapper objectMapper;
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
            throw new VaultAccessException("Vault " + documentEntity.getVault() + " is not found!");
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
     */
    public void deleteDocument(final DocumentEntity documentEntity) {
        if (!vaultLocations.containsKey(documentEntity.getVault())) {
            throw new IllegalStateException("Vault " + documentEntity.getVault() + " is not found!");
        }

        final VaultLocation vaultLocation = vaultLocations.get(documentEntity.getVault());

        final Request request = new Request.Builder()
                .url("http://" + vaultLocation.getLocation() + ":" + vaultLocation.getPort() + "/document/" + documentEntity.getId())
                .delete()
                .build();

        try {
            okHttpClient.newCall(request)
                    .execute()
                    .close();
        } catch (final IOException e) {
            throw new VaultAccessException("Error while connecting to the vault for document:  " + documentEntity.getId() + "!", e);
        }
    }

    @SneakyThrows
    public void recompressDocument(final DocumentEntity documentEntity, final DocumentCompression documentCompression) {
        if (!vaultLocations.containsKey(documentEntity.getVault())) {
            throw new IllegalStateException("Vault " + documentEntity.getVault() + " is not found!");
        }

        final VaultLocation vaultLocation = vaultLocations.get(documentEntity.getVault());

        final Request request = new Request.Builder()
                .url("http://" + vaultLocation.getLocation() + ":" + vaultLocation.getPort() + "/document/"
                        + documentEntity.getId() + "/recompress")
                .put(
                        RequestBody.create(
                                objectMapper.writeValueAsBytes(
                                        RecompressDocumentRequest.builder()
                                                .compression(documentCompression)
                                                .build()
                                ),
                                JSON_MEDIA_TYPE
                        )
                )
                .build();

        try {
            okHttpClient.newCall(request)
                    .execute()
                    .close();
        } catch (final IOException e) {
            throw new VaultAccessException("Error while connecting to the vault for document:  " + documentEntity.getId() + "!", e);
        }
    }

    public boolean documentExists(final DocumentEntity documentEntity) {
        if (!vaultLocations.containsKey(documentEntity.getVault())) {
            throw new IllegalStateException("Vault " + documentEntity.getVault() + " is not found!");
        }

        final VaultLocation vaultLocation = vaultLocations.get(documentEntity.getVault());

        final Request request = new Request.Builder()
                .url("http://" + vaultLocation.getLocation() + ":" + vaultLocation.getPort() + "/document/"
                        + documentEntity.getId() + "/exists")
                .get()
                .build();

        try {
            final String response = okHttpClient.newCall(request)
                    .execute()
                    .body()
                    .string();

            return objectMapper.readValue(response, DocumentExistsResponse.class)
                    .isExists();
        } catch (final IOException e) {
            throw new VaultAccessException("Error while connecting to the vault for document:  " + documentEntity.getId() + "!", e);
        }
    }

    @SneakyThrows
    public void replaceCorruptDocument(final DocumentEntity documentEntity, final byte[] content) {
        if (!vaultLocations.containsKey(documentEntity.getVault())) {
            throw new IllegalStateException("Vault " + documentEntity.getVault() + " is not found!");
        }

        final VaultLocation vaultLocation = vaultLocations.get(documentEntity.getVault());

        final Request request = new Request.Builder()
                .url("http://" + vaultLocation.getLocation() + ":" + vaultLocation.getPort() + "/document/"
                        + documentEntity.getId() + "/replace")
                .put(
                        RequestBody.create(
                                objectMapper.writeValueAsBytes(
                                        ReplaceCorruptDocumentRequest.builder()
                                                .content(content)
                                                .build()
                                )
                        )
                )
                .build();

        try {
            okHttpClient.newCall(request)
                    .execute()
                    .close();
        } catch (final IOException e) {
            throw new VaultAccessException("Error while connecting to the vault for document:  " + documentEntity.getId() + "!", e);
        }
    }
}
