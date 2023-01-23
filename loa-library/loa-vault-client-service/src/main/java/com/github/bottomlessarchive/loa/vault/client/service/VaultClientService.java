package com.github.bottomlessarchive.loa.vault.client.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bottomlessarchive.loa.compression.domain.DocumentCompression;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.vault.client.service.domain.VaultAccessException;
import com.github.bottomlessarchive.loa.vault.client.service.domain.VaultLocation;
import com.github.bottomlessarchive.loa.vault.client.service.request.RecompressDocumentRequest;
import com.github.bottomlessarchive.loa.vault.client.service.response.DocumentExistsResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class VaultClientService {

    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient vaultOkHttpClient;
    private final ObjectMapper objectMapper;
    private final VaultLocationContainer vaultLocationContainer;

    /**
     * Requests and returns the content of a document. If the document is not found or an error happened while doing
     * the request, an {@link VaultAccessException} will be thrown.
     * <p>
     * The returned {@link InputStream} should be closed after processed to avoid leaking resources.
     *
     * @param documentEntity the document entity to get the content for
     * @return the content of the document
     */
    public InputStream queryDocument(final DocumentEntity documentEntity) {
        final VaultLocation vaultLocation = vaultLocationContainer.getVaultLocation(documentEntity.getVault())
                .orElseThrow(() -> new VaultAccessException("Vault " + documentEntity.getVault() + " is not found!"));

        final Request request = new Request.Builder()
                .url("http://" + vaultLocation.getLocation() + ":" + vaultLocation.getPort() + "/document/" + documentEntity.getId())
                .get()
                .build();

        try {
            return vaultOkHttpClient.newCall(request)
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
        final VaultLocation vaultLocation = vaultLocationContainer.getVaultLocation(documentEntity.getVault())
                .orElseThrow(() -> new VaultAccessException("Vault " + documentEntity.getVault() + " is not found!"));

        final Request request = new Request.Builder()
                .url("http://" + vaultLocation.getLocation() + ":" + vaultLocation.getPort() + "/document/" + documentEntity.getId())
                .delete()
                .build();

        try {
            vaultOkHttpClient.newCall(request)
                    .execute()
                    .close();
        } catch (final IOException e) {
            throw new VaultAccessException("Error while connecting to the vault for document:  " + documentEntity.getId() + "!", e);
        }
    }

    @SneakyThrows
    public void recompressDocument(final DocumentEntity documentEntity, final DocumentCompression documentCompression) {
        final VaultLocation vaultLocation = vaultLocationContainer.getVaultLocation(documentEntity.getVault())
                .orElseThrow(() -> new VaultAccessException("Vault " + documentEntity.getVault() + " is not found!"));

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
            vaultOkHttpClient.newCall(request)
                    .execute()
                    .close();
        } catch (final IOException e) {
            throw new VaultAccessException("Error while connecting to the vault for document:  " + documentEntity.getId() + "!", e);
        }
    }

    public boolean documentExists(final DocumentEntity documentEntity) {
        final VaultLocation vaultLocation = vaultLocationContainer.getVaultLocation(documentEntity.getVault())
                .orElseThrow(() -> new VaultAccessException("Vault " + documentEntity.getVault() + " is not found!"));

        final Request request = new Request.Builder()
                .url("http://" + vaultLocation.getLocation() + ":" + vaultLocation.getPort() + "/document/"
                        + documentEntity.getId() + "/exists")
                .get()
                .build();

        try {
            final String response = vaultOkHttpClient.newCall(request)
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
        final VaultLocation vaultLocation = vaultLocationContainer.getVaultLocation(documentEntity.getVault())
                .orElseThrow(() -> new VaultAccessException("Vault " + documentEntity.getVault() + " is not found!"));

        final Request request = new Request.Builder()
                .url("http://" + vaultLocation.getLocation() + ":" + vaultLocation.getPort() + "/document/"
                        + documentEntity.getId() + "/replace")
                .put(
                        new MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                .addFormDataPart("replacementFile", documentEntity.getId().toString(),
                                        RequestBody.create(content, MediaType.parse("application/octet-stream")))
                                .build()
                )
                .build();

        try {
            vaultOkHttpClient.newCall(request)
                    .execute()
                    .close();
        } catch (final IOException e) {
            throw new VaultAccessException("Error while connecting to the vault for document:  " + documentEntity.getId() + "!", e);
        }
    }
}
