package com.github.bottomlessarchive.loa.vault.client.service;

import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.vault.client.service.domain.VaultAccessException;
import com.github.bottomlessarchive.loa.vault.client.service.domain.VaultLocation;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VaultClientServiceTest {

    private static final UUID TEST_DOCUMENT_ID = UUID.fromString("123e4567-e89b-12d3-a456-556642440000");

    @InjectMocks
    private VaultClientService vaultClientService;

    @Mock
    private OkHttpClient okHttpClient;

    @Mock
    private VaultLocationContainer vaultLocationContainer;

    @Captor
    private ArgumentCaptor<Request> requestArgumentCaptor;

    @Test
    void testQueryDocumentWhenDocumentIsInDifferentVault() {
        final DocumentEntity documentEntity = DocumentEntity.builder()
                .id(TEST_DOCUMENT_ID)
                .vault("default-2")
                .build();

        assertThrows(VaultAccessException.class, () -> vaultClientService.queryDocument(documentEntity));
    }

    @Test
    void testQueryDocumentWhenTheRequestWasSuccessful() throws IOException {
        when(vaultLocationContainer.getVaultLocation("default"))
                .thenReturn(
                        Optional.of(
                                VaultLocation.builder()
                                        .location("testlocation")
                                        .port(123)
                                        .build()
                        )
                );
        final DocumentEntity documentEntity = DocumentEntity.builder()
                .id(TEST_DOCUMENT_ID)
                .vault("default")
                .build();

        final Call call = Mockito.mock(Call.class);
        when(okHttpClient.newCall(any()))
                .thenReturn(call);
        final Response response = Mockito.mock(Response.class);
        when(call.execute())
                .thenReturn(response);
        final ResponseBody responseBody = Mockito.mock(ResponseBody.class);
        when(response.body())
                .thenReturn(responseBody);
        final InputStream byteStream = new ByteArrayInputStream(new byte[]{});
        when(responseBody.byteStream())
                .thenReturn(byteStream);

        final InputStream result = vaultClientService.queryDocument(documentEntity);

        assertThat(result).hasBinaryContent(new byte[]{});
        Mockito.verify(okHttpClient).newCall(requestArgumentCaptor.capture());

        final Request request = requestArgumentCaptor.getValue();
        assertThat(request.url().url().toString()).isEqualTo("http://testlocation:123/document/123e4567-e89b-12d3-a456-556642440000");
        assertThat(request.method()).isEqualTo("GET");
        assertThat(result).isSameAs(byteStream);
    }
}
