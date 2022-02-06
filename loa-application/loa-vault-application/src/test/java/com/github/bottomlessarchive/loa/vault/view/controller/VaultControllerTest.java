package com.github.bottomlessarchive.loa.vault.view.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bottomlessarchive.loa.compression.domain.DocumentCompression;
import com.github.bottomlessarchive.loa.document.service.DocumentManipulator;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentType;
import com.github.bottomlessarchive.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.bottomlessarchive.loa.vault.configuration.VaultConfigurationProperties;
import com.github.bottomlessarchive.loa.vault.service.RecompressorService;
import com.github.bottomlessarchive.loa.vault.service.VaultDocumentManager;
import com.github.bottomlessarchive.loa.vault.service.backend.service.VaultDocumentStorage;
import com.github.bottomlessarchive.loa.vault.service.location.VaultLocation;
import com.github.bottomlessarchive.loa.vault.service.location.VaultLocationFactory;
import com.github.bottomlessarchive.loa.vault.view.request.domain.RecompressDocumentRequest;
import com.github.bottomlessarchive.loa.vault.view.request.domain.ReplaceDocumentRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
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
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@AutoConfigureMockMvc
@EnableAutoConfiguration
@SpringBootTest(classes = VaultController.class)
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
        when(documentEntityFactory.getDocumentEntity(UUID.fromString(TEST_DOCUMENT_ID)))
                .thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/document/" + TEST_DOCUMENT_ID))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Document not found with id 123e4567-e89b-12d3-a456-556642440000 or already removed!"));
    }

    @Test
    @SneakyThrows
    void testQueryDocumentWhenDocumentIsInADifferentVault() {
        when(vaultConfigurationProperties.name())
                .thenReturn("my-vault");
        when(documentEntityFactory.getDocumentEntity(UUID.fromString(TEST_DOCUMENT_ID)))
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
        when(vaultConfigurationProperties.name())
                .thenReturn("my-vault");
        final DocumentEntity documentEntity = DocumentEntity.builder()
                .vault("my-vault")
                .type(DocumentType.PDF)
                .build();
        when(documentEntityFactory.getDocumentEntity(UUID.fromString(TEST_DOCUMENT_ID)))
                .thenReturn(Optional.of(documentEntity));
        final Resource documentResource = new ByteArrayResource(new byte[]{0, 1, 2});
        when(vaultDocumentManager.readDocument(documentEntity))
                .thenReturn(documentResource);

        mockMvc.perform(MockMvcRequestBuilders.get("/document/" + TEST_DOCUMENT_ID))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(content().bytes(new byte[]{0, 1, 2}));
    }

    @Test
    @SneakyThrows
    void testRecompressWhenModificationsAreDisabled() {
        when(vaultConfigurationProperties.modificationEnabled())
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
        when(vaultConfigurationProperties.modificationEnabled())
                .thenReturn(true);
        when(documentEntityFactory.getDocumentEntity(UUID.fromString(TEST_DOCUMENT_ID)))
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
        when(vaultConfigurationProperties.modificationEnabled())
                .thenReturn(true);
        when(vaultConfigurationProperties.name())
                .thenReturn("my-vault");
        when(documentEntityFactory.getDocumentEntity(UUID.fromString(TEST_DOCUMENT_ID)))
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
        when(vaultConfigurationProperties.modificationEnabled())
                .thenReturn(true);
        when(vaultConfigurationProperties.name())
                .thenReturn("my-vault");
        final DocumentEntity documentEntity = DocumentEntity.builder()
                .vault("my-vault")
                .build();
        when(documentEntityFactory.getDocumentEntity(UUID.fromString(TEST_DOCUMENT_ID)))
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

        verify(recompressorService).recompress(documentEntity, DocumentCompression.GZIP);
    }

    @Test
    @SneakyThrows
    void testRemoveWhenModificationsAreDisabled() {
        when(vaultConfigurationProperties.modificationEnabled())
                .thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.delete("/document/" + TEST_DOCUMENT_ID))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Modification is disabled on this vault instance!"));
    }

    @Test
    @SneakyThrows
    void testRemoveWhenDocumentNotFound() {
        when(vaultConfigurationProperties.modificationEnabled())
                .thenReturn(true);
        when(documentEntityFactory.getDocumentEntity(UUID.fromString(TEST_DOCUMENT_ID)))
                .thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.delete("/document/" + TEST_DOCUMENT_ID))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Document not found with id 123e4567-e89b-12d3-a456-556642440000 or already removed!"));
    }

    @Test
    @SneakyThrows
    void testRemoveWhenDocumentIsInADifferentVault() {
        when(vaultConfigurationProperties.modificationEnabled())
                .thenReturn(true);
        when(vaultConfigurationProperties.name())
                .thenReturn("my-vault");
        when(documentEntityFactory.getDocumentEntity(UUID.fromString(TEST_DOCUMENT_ID)))
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
        when(vaultConfigurationProperties.modificationEnabled())
                .thenReturn(true);
        when(vaultConfigurationProperties.name())
                .thenReturn("my-vault");
        final DocumentEntity documentEntity = DocumentEntity.builder()
                .id(UUID.fromString(TEST_DOCUMENT_ID))
                .vault("my-vault")
                .build();
        when(documentEntityFactory.getDocumentEntity(UUID.fromString(TEST_DOCUMENT_ID)))
                .thenReturn(Optional.of(documentEntity));

        mockMvc.perform(MockMvcRequestBuilders.delete("/document/" + TEST_DOCUMENT_ID))
                .andExpect(status().isOk());

        verify(vaultDocumentManager).removeDocument(documentEntity);
        verify(documentEntityFactory).removeDocumentEntity(documentEntity);
    }

    @Test
    @SneakyThrows
    void testReplaceCorruptDocumentWhenModificationsAreDisabled() {
        when(vaultConfigurationProperties.modificationEnabled())
                .thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.put("/document/" + TEST_DOCUMENT_ID + "/replace")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(
                                ReplaceDocumentRequest.builder()
                                        .content(new byte[]{})
                                        .build()
                        ))
                )
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Modification is disabled on this vault instance!"));
    }

    @Test
    @SneakyThrows
    void testReplaceCorruptDocumentWhenDocumentIsInADifferentVault() {
        when(vaultConfigurationProperties.modificationEnabled())
                .thenReturn(true);
        when(vaultConfigurationProperties.name())
                .thenReturn("my-vault");
        when(documentEntityFactory.getDocumentEntity(UUID.fromString(TEST_DOCUMENT_ID)))
                .thenReturn(
                        Optional.of(
                                DocumentEntity.builder()
                                        .vault("different-vault")
                                        .build()
                        )
                );

        mockMvc.perform(MockMvcRequestBuilders.put("/document/" + TEST_DOCUMENT_ID + "/replace")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(
                                ReplaceDocumentRequest.builder()
                                        .content(new byte[]{})
                                        .build()
                        ))
                )
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Document with id 123e4567-e89b-12d3-a456-556642440000 is available on a different vault!"));
    }

    @Test
    @SneakyThrows
    void testReplaceCorruptDocumentReplacesTheDocument() {
        when(vaultConfigurationProperties.modificationEnabled())
                .thenReturn(true);
        when(vaultConfigurationProperties.name())
                .thenReturn("my-vault");
        final DocumentEntity documentEntity = DocumentEntity.builder()
                .id(UUID.fromString(TEST_DOCUMENT_ID))
                .vault("my-vault")
                .compression(DocumentCompression.GZIP)
                .build();
        when(documentEntityFactory.getDocumentEntity(UUID.fromString(TEST_DOCUMENT_ID)))
                .thenReturn(Optional.of(documentEntity));
        final VaultLocation vaultLocation = mock(VaultLocation.class);
        when(vaultLocationFactory.getLocation(documentEntity, documentEntity.getCompression()))
                .thenReturn(vaultLocation);
        when(documentManipulator.markDownloaded(UUID.fromString(TEST_DOCUMENT_ID)))
                .thenReturn(Mono.empty());

        final byte[] newDocumentContent = {1, 2, 3, 4};


        mockMvc.perform(MockMvcRequestBuilders.put("/document/" + TEST_DOCUMENT_ID + "/replace")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(
                                ReplaceDocumentRequest.builder()
                                        .content(newDocumentContent)
                                        .build()
                        ))
                )
                .andExpect(status().isOk());

        verify(vaultDocumentStorage)
                .persistDocument(documentEntity, newDocumentContent, vaultLocation);
        verify(documentManipulator)
                .markDownloadedSync(UUID.fromString(TEST_DOCUMENT_ID));
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
