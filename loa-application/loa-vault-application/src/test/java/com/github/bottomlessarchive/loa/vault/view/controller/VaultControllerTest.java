package com.github.bottomlessarchive.loa.vault.view.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bottomlessarchive.loa.compression.domain.DocumentCompression;
import com.github.bottomlessarchive.loa.document.service.DocumentManipulator;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentType;
import com.github.bottomlessarchive.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.bottomlessarchive.loa.document.view.service.MediaTypeCalculator;
import com.github.bottomlessarchive.loa.vault.configuration.VaultConfigurationProperties;
import com.github.bottomlessarchive.loa.vault.service.RecompressorService;
import com.github.bottomlessarchive.loa.vault.service.VaultDocumentManager;
import com.github.bottomlessarchive.loa.vault.service.backend.service.VaultDocumentStorage;
import com.github.bottomlessarchive.loa.vault.service.location.VaultLocation;
import com.github.bottomlessarchive.loa.vault.service.location.VaultLocationFactory;
import com.github.bottomlessarchive.loa.vault.view.request.domain.RecompressDocumentRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@AutoConfigureMockMvc
@EnableAutoConfiguration
@SpringBootTest(classes = {VaultController.class, MediaTypeCalculator.class})
class VaultControllerTest {

    private static final String TEST_DOCUMENT_ID = "123e4567-e89b-12d3-a456-556642440000";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentEntityFactory documentEntityFactory;

    @MockBean
    private VaultDocumentManager vaultDocumentManager;

    @MockBean
    private RecompressorService recompressorService;

    @MockBean
    private VaultConfigurationProperties vaultConfigurationProperties;

    @MockBean
    private DocumentManipulator documentManipulator;

    @MockBean
    private VaultLocationFactory vaultLocationFactory;

    @MockBean
    private VaultDocumentStorage vaultDocumentStorage;

    @Test
    @SneakyThrows
    void testQueryDocumentWhenDocumentNotFound() {
        Mockito.when(documentEntityFactory.getDocumentEntity(UUID.fromString(TEST_DOCUMENT_ID)))
                .thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/document/" + TEST_DOCUMENT_ID))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Document not found with id 123e4567-e89b-12d3-a456-556642440000 or already removed!"));
    }

    @Test
    @SneakyThrows
    void testQueryDocumentWhenDocumentIsInADifferentVault() {
        Mockito.when(vaultConfigurationProperties.name())
                .thenReturn("my-vault");
        Mockito.when(documentEntityFactory.getDocumentEntity(UUID.fromString(TEST_DOCUMENT_ID)))
                .thenReturn(Optional.of(
                                DocumentEntity.builder()
                                        .vault("different-vault")
                                        .build()
                        )
                );

        mockMvc.perform(MockMvcRequestBuilders.get("/document/" + TEST_DOCUMENT_ID))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Document with id 123e4567-e89b-12d3-a456-556642440000 is available on a different vault!"));
    }

    @Test
    @SneakyThrows
    void testQueryDocumentWhenRequestIsSuccessful() {
        Mockito.when(vaultConfigurationProperties.name())
                .thenReturn("my-vault");
        final DocumentEntity documentEntity = DocumentEntity.builder()
                .vault("my-vault")
                .type(DocumentType.PDF)
                .build();
        Mockito.when(documentEntityFactory.getDocumentEntity(UUID.fromString(TEST_DOCUMENT_ID)))
                .thenReturn(Optional.of(documentEntity));
        final Resource documentResource = new ByteArrayResource(new byte[]{0, 1, 2});
        Mockito.when(vaultDocumentManager.readDocument(documentEntity))
                .thenReturn(documentResource);

        mockMvc.perform(MockMvcRequestBuilders.get("/document/" + TEST_DOCUMENT_ID))
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(content().bytes(new byte[]{0, 1, 2}));
    }

    @Test
    @SneakyThrows
    void testRecompressWhenModificationsAreDisabled() {
        Mockito.when(vaultConfigurationProperties.modificationEnabled())
                .thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.put("/document/" + TEST_DOCUMENT_ID + "/recompress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(
                                RecompressDocumentRequest.builder()
                                        .compression(DocumentCompression.GZIP)
                                        .build()
                        ))
                )
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Modification is disabled on this vault instance!"));
    }

    @Test
    @SneakyThrows
    void testRecompressWhenDocumentNotFound() {
        Mockito.when(vaultConfigurationProperties.modificationEnabled())
                .thenReturn(true);
        Mockito.when(documentEntityFactory.getDocumentEntity(UUID.fromString(TEST_DOCUMENT_ID)))
                .thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.put("/document/" + TEST_DOCUMENT_ID + "/recompress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(
                                RecompressDocumentRequest.builder()
                                        .compression(DocumentCompression.GZIP)
                                        .build()
                        ))
                )
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Document not found with id 123e4567-e89b-12d3-a456-556642440000!"));
    }

    @Test
    @SneakyThrows
    void testRecompressWhenDocumentIsInADifferentVault() {
        Mockito.when(vaultConfigurationProperties.modificationEnabled())
                .thenReturn(true);
        Mockito.when(vaultConfigurationProperties.name())
                .thenReturn("my-vault");
        Mockito.when(documentEntityFactory.getDocumentEntity(UUID.fromString(TEST_DOCUMENT_ID)))
                .thenReturn(
                        Optional.of(
                                DocumentEntity.builder()
                                        .vault("different-vault")
                                        .build()
                        )
                );

        mockMvc.perform(MockMvcRequestBuilders.put("/document/" + TEST_DOCUMENT_ID + "/recompress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(
                                RecompressDocumentRequest.builder()
                                        .compression(DocumentCompression.GZIP)
                                        .build()
                        ))
                )
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Document with id 123e4567-e89b-12d3-a456-556642440000 is available on a different vault!"));
    }

    @Test
    @SneakyThrows
    void testRecompressWhenRequestIsSuccessful() {
        Mockito.when(vaultConfigurationProperties.modificationEnabled())
                .thenReturn(true);
        Mockito.when(vaultConfigurationProperties.name())
                .thenReturn("my-vault");
        final DocumentEntity documentEntity = DocumentEntity.builder()
                .vault("my-vault")
                .build();
        Mockito.when(documentEntityFactory.getDocumentEntity(UUID.fromString(TEST_DOCUMENT_ID)))
                .thenReturn(Optional.of(documentEntity));

        mockMvc.perform(MockMvcRequestBuilders.put("/document/" + TEST_DOCUMENT_ID + "/recompress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(
                                RecompressDocumentRequest.builder()
                                        .compression(DocumentCompression.GZIP)
                                        .build()
                        ))
                )
                .andExpect(status().isOk());

        Mockito.verify(recompressorService).recompress(documentEntity, DocumentCompression.GZIP);
    }

    @Test
    @SneakyThrows
    void testRemoveWhenModificationsAreDisabled() {
        Mockito.when(vaultConfigurationProperties.modificationEnabled())
                .thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.delete("/document/" + TEST_DOCUMENT_ID))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Modification is disabled on this vault instance!"));
    }

    @Test
    @SneakyThrows
    void testRemoveWhenDocumentNotFound() {
        Mockito.when(vaultConfigurationProperties.modificationEnabled())
                .thenReturn(true);
        Mockito.when(documentEntityFactory.getDocumentEntity(UUID.fromString(TEST_DOCUMENT_ID)))
                .thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.delete("/document/" + TEST_DOCUMENT_ID))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Document not found with id 123e4567-e89b-12d3-a456-556642440000 or already removed!"));
    }

    @Test
    @SneakyThrows
    void testRemoveWhenDocumentIsInADifferentVault() {
        Mockito.when(vaultConfigurationProperties.modificationEnabled())
                .thenReturn(true);
        Mockito.when(vaultConfigurationProperties.name())
                .thenReturn("my-vault");
        Mockito.when(documentEntityFactory.getDocumentEntity(UUID.fromString(TEST_DOCUMENT_ID)))
                .thenReturn(
                        Optional.of(
                                DocumentEntity.builder()
                                        .vault("different-vault")
                                        .build()
                        )
                );

        mockMvc.perform(MockMvcRequestBuilders.delete("/document/" + TEST_DOCUMENT_ID))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Document with id 123e4567-e89b-12d3-a456-556642440000 is available on a different vault!"));
    }

    @Test
    @SneakyThrows
    void testRemoveWhenRequestIsSuccessful() {
        Mockito.when(vaultConfigurationProperties.modificationEnabled())
                .thenReturn(true);
        Mockito.when(vaultConfigurationProperties.name())
                .thenReturn("my-vault");
        final DocumentEntity documentEntity = DocumentEntity.builder()
                .id(UUID.fromString(TEST_DOCUMENT_ID))
                .vault("my-vault")
                .build();
        Mockito.when(documentEntityFactory.getDocumentEntity(UUID.fromString(TEST_DOCUMENT_ID)))
                .thenReturn(Optional.of(documentEntity));

        mockMvc.perform(MockMvcRequestBuilders.delete("/document/" + TEST_DOCUMENT_ID))
                .andExpect(status().isOk());

        Mockito.verify(vaultDocumentManager).removeDocument(documentEntity);
        Mockito.verify(documentEntityFactory).removeDocumentEntity(documentEntity);
    }

    @Test
    @SneakyThrows
    void testReplaceCorruptDocumentWhenModificationsAreDisabled() {
        Mockito.when(vaultConfigurationProperties.modificationEnabled())
                .thenReturn(false);

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/document/" + TEST_DOCUMENT_ID + "/replace")
                                .file("replacementFile", new byte[]{})
                                .with(request -> {
                                    request.setMethod("PUT");
                                    return request;
                                })
                )
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Modification is disabled on this vault instance!"));
    }

    @Test
    @SneakyThrows
    void testReplaceCorruptDocumentWhenDocumentIsInADifferentVault() {
        Mockito.when(vaultConfigurationProperties.modificationEnabled())
                .thenReturn(true);
        Mockito.when(vaultConfigurationProperties.name())
                .thenReturn("my-vault");
        Mockito.when(documentEntityFactory.getDocumentEntity(UUID.fromString(TEST_DOCUMENT_ID)))
                .thenReturn(
                        Optional.of(
                                DocumentEntity.builder()
                                        .vault("different-vault")
                                        .build()
                        )
                );

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/document/" + TEST_DOCUMENT_ID + "/replace")
                                .file("replacementFile", new byte[]{})
                                .with(request -> {
                                    request.setMethod("PUT");
                                    return request;
                                })
                )
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Document with id 123e4567-e89b-12d3-a456-556642440000 is available on a different vault!"));
    }

    @Test
    @SneakyThrows
    void testReplaceCorruptDocumentReplacesTheDocument() {
        Mockito.when(vaultConfigurationProperties.modificationEnabled())
                .thenReturn(true);
        Mockito.when(vaultConfigurationProperties.name())
                .thenReturn("my-vault");
        final DocumentEntity documentEntity = DocumentEntity.builder()
                .id(UUID.fromString(TEST_DOCUMENT_ID))
                .vault("my-vault")
                .compression(DocumentCompression.GZIP)
                .build();
        Mockito.when(documentEntityFactory.getDocumentEntity(UUID.fromString(TEST_DOCUMENT_ID)))
                .thenReturn(Optional.of(documentEntity));
        final VaultLocation vaultLocation = Mockito.mock(VaultLocation.class);
        Mockito.when(vaultLocationFactory.getLocation(documentEntity, documentEntity.getCompression()))
                .thenReturn(vaultLocation);

        final byte[] newDocumentContent = {1, 2, 3, 4};
        final ArgumentCaptor<InputStream> inputStreamArgumentCaptor = ArgumentCaptor.forClass(InputStream.class);

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/document/" + TEST_DOCUMENT_ID + "/replace")
                                .file("replacementFile", newDocumentContent)
                                .with(request -> {
                                    request.setMethod("PUT");
                                    return request;
                                })
                )
                .andExpect(status().isOk());

        Mockito.verify(vaultDocumentStorage)
                .persistDocument(eq(documentEntity), inputStreamArgumentCaptor.capture(), eq(vaultLocation), eq(4L));
        assertThat(inputStreamArgumentCaptor.getValue().readAllBytes())
                .isEqualTo(newDocumentContent);
        Mockito.verify(documentManipulator)
                .markDownloaded(UUID.fromString(TEST_DOCUMENT_ID));
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
