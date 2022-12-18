package com.github.bottomlessarchive.loa.vault.integration;

import com.github.bottomlessarchive.loa.application.domain.ApplicationType;
import com.github.bottomlessarchive.loa.compression.domain.DocumentCompression;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentStatus;
import com.github.bottomlessarchive.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.bottomlessarchive.loa.document.service.entity.factory.domain.DocumentCreationContext;
import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import com.github.bottomlessarchive.loa.vault.service.location.file.FileFactory;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.thomaskasene.wiremock.junit.WireMockStubs;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.ByteArrayInputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static com.github.bottomlessarchive.loa.conductor.service.ConductorClientTestUtility.expectQueryServiceCall;
import static com.github.bottomlessarchive.loa.conductor.service.ConductorClientTestUtility.expectRegisterServiceCall;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@WireMockStubs
@Testcontainers
@SpringBootTest(
        properties = {
                "loa.conductor.port=2002",
                "loa.vault.archiving=false",
                "loa.vault.location.file.path=/vault/"
        }
)
@WireMockTest(httpPort = 2002)
@AutoConfigureMockMvc
class VaultViewDefaultIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DocumentEntityFactory documentEntityFactory;

    @MockBean
    private FileFactory fileFactory;

    private FileSystem fileSystem;

    //TODO: Why does the application connect to the queue even if loa.vault.archiving is disabled?
    @Container
    private static final GenericContainer<?> ARTEMIS_CONTAINER = new GenericContainer<>("vromero/activemq-artemis:2.16.0")
            .withExposedPorts(61616)
            .withStartupTimeout(Duration.ofMinutes(5))
            .withLogConsumer(new Slf4jLogConsumer(log).withPrefix("AMQ-LOG"))
            .withEnv("DISABLE_SECURITY", "true")
            .withEnv("BROKER_CONFIG_GLOBAL_MAX_SIZE", "50000")
            .withEnv("BROKER_CONFIG_MAX_SIZE_BYTES", "50000")
            .withEnv("BROKER_CONFIG_MAX_DISK_USAGE", "100");

    @Container
    private static final MongoDBContainer MONGO_CONTAINER = new MongoDBContainer("mongo:6.0.1")
            .withStartupTimeout(Duration.ofMinutes(5))
            .withLogConsumer(new Slf4jLogConsumer(log).withPrefix("MONGO-LOG"));

    @BeforeAll
    static void setup() {
        expectStartupServiceCalls();
    }

    @BeforeEach
    public void setupEach() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
    }

    @AfterEach
    @SneakyThrows
    public void teardownEach() {
        fileSystem.close();
    }

    @Test
    void testQueryDocumentWhenDocumentIsInDifferentVault() throws Exception {
        final UUID documentId = UUID.randomUUID();

        documentEntityFactory.newDocumentEntity(
                DocumentCreationContext.builder()
                        .id(documentId)
                        .type(DocumentType.PDF)
                        .status(DocumentStatus.DOWNLOADED)
                        .checksum("ba7916bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad")
                        .compression(DocumentCompression.NONE)
                        .vault("not-this-one")
                        .fileSize(123)
                        .source("test-source")
                        .sourceLocationId(Optional.empty())
                        .build()
        );

        mockMvc.perform(get("/document/" + documentId))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Document with id " + documentId + " is available on a different vault!"));
    }

    @Test
    void testQueryDocumentWhenDocumentIsInVault() throws Exception {
        final UUID documentId = UUID.randomUUID();

        documentEntityFactory.newDocumentEntity(
                DocumentCreationContext.builder()
                        .id(documentId)
                        .type(DocumentType.PDF)
                        .status(DocumentStatus.DOWNLOADED)
                        .checksum("ba8016bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad")
                        .compression(DocumentCompression.NONE)
                        .vault("default")
                        .fileSize(123)
                        .source("test-source")
                        .sourceLocationId(Optional.empty())
                        .build()
        );

        final Path fakeDocumentContent = setupFakeFile("/vault/" + documentId + ".pdf", new byte[]{1, 2, 3, 4});

        when(fileFactory.newFile("/vault/", documentId + ".pdf"))
                .thenReturn(fakeDocumentContent);

        mockMvc.perform(get("/document/" + documentId))
                .andExpect(status().isOk())
                .andExpect(content().bytes(new byte[]{1, 2, 3, 4}))
                .andExpect(header().stringValues("Content-Type", "application/pdf"));
    }

    @Test
    void testQueryDocumentWhenDocumentNotFoundInDatabase() throws Exception {
        final UUID documentId = UUID.randomUUID();

        // Do not insert the entity's data to the database

        mockMvc.perform(get("/document/" + documentId))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Document not found with id " + documentId + " or already removed!"));
    }

    @Test
    void testDeleteDocumentWhenDocumentIsInDifferentVault() throws Exception {
        final UUID documentId = UUID.randomUUID();

        documentEntityFactory.newDocumentEntity(
                DocumentCreationContext.builder()
                        .id(documentId)
                        .type(DocumentType.PDF)
                        .status(DocumentStatus.DOWNLOADED)
                        .checksum("ba7926bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad")
                        .compression(DocumentCompression.NONE)
                        .vault("not-this-one")
                        .fileSize(123)
                        .source("test-source")
                        .sourceLocationId(Optional.empty())
                        .build()
        );

        mockMvc.perform(delete("/document/" + documentId))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Document with id " + documentId + " is available on a different vault!"));
    }

    @Test
    void testDeleteDocumentWhenDocumentNotFoundInDatabase() throws Exception {
        final UUID documentId = UUID.randomUUID();

        mockMvc.perform(delete("/document/" + documentId))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Document not found with id " + documentId + " or already removed!"));
    }

    @Test
    void testDeleteDocumentWhenDocumentIsInVault() throws Exception {
        final UUID documentId = UUID.randomUUID();

        documentEntityFactory.newDocumentEntity(
                DocumentCreationContext.builder()
                        .id(documentId)
                        .type(DocumentType.PDF)
                        .status(DocumentStatus.DOWNLOADED)
                        .checksum("ba8019bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad")
                        .compression(DocumentCompression.NONE)
                        .vault("default")
                        .fileSize(123)
                        .source("test-source")
                        .sourceLocationId(Optional.empty())
                        .build()
        );

        final Path fakeDocumentContent = setupFakeFile("/vault/" + documentId + ".pdf", new byte[]{1, 2, 3, 4});

        when(fileFactory.newFile("/vault/", documentId + ".pdf"))
                .thenReturn(fakeDocumentContent);

        mockMvc.perform(delete("/document/" + documentId))
                .andExpect(status().isOk());

        assertThat(fakeDocumentContent)
                .doesNotExist();
        assertThat(documentEntityFactory.getDocumentEntity(documentId))
                .isEmpty();
    }

    @SneakyThrows
    private Path setupFakeFile(final String fileNameAndPath, final byte[] testFileContent) {
        final Path testFilePath = fileSystem.getPath(fileNameAndPath);

        Files.createDirectories(testFilePath.getParent());
        Files.copy(new ByteArrayInputStream(testFileContent), testFilePath);

        return testFilePath;
    }

    private static void expectStartupServiceCalls() {
        expectRegisterServiceCall(ApplicationType.VAULT_APPLICATION);

        expectQueryServiceCall(ApplicationType.DOCUMENT_DATABASE, "127.0.0.1", MONGO_CONTAINER.getFirstMappedPort());
        expectQueryServiceCall(ApplicationType.QUEUE_APPLICATION, "127.0.0.1", ARTEMIS_CONTAINER.getFirstMappedPort());
    }
}
