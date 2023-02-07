package com.github.bottomlessarchive.loa.vault.integration;

import com.github.bottomlessarchive.loa.application.domain.ApplicationType;
import com.github.bottomlessarchive.loa.compression.domain.DocumentCompression;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentStatus;
import com.github.bottomlessarchive.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.bottomlessarchive.loa.document.service.entity.factory.domain.DocumentCreationContext;
import com.github.bottomlessarchive.loa.stage.service.StageLocationFactory;
import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import com.github.bottomlessarchive.loa.vault.service.location.file.configuration.FileConfigurationProperties;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.thomaskasene.wiremock.junit.WireMockStubs;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import wiremock.org.apache.commons.io.file.PathUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static com.github.bottomlessarchive.loa.conductor.service.ConductorClientTestUtility.expectQueryServiceCall;
import static com.github.bottomlessarchive.loa.conductor.service.ConductorClientTestUtility.expectRegisterServiceCall;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
                "loa.vault.staging-directory=/stage/",
                "loa.vault.location.file.path=/vault/"
        }
)
@WireMockTest(httpPort = 2002)
@AutoConfigureMockMvc
class VaultViewDefaultIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Captor
    private ArgumentCaptor<UUID> uuidArgumentCaptor;

    @SpyBean
    private StageLocationFactory stageLocationFactory;

    @Autowired
    private DocumentEntityFactory documentEntityFactory;

    private static final FileSystem FILE_SYSTEM = Jimfs.newFileSystem(Configuration.unix());

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

    @TestConfiguration
    public static class ReplacementConfiguration {

        @Bean
        @Primary
        public FileConfigurationProperties fileConfigurationProperties() throws IOException {
            Files.createDirectories(FILE_SYSTEM.getPath("/vault"));

            return new FileConfigurationProperties(FILE_SYSTEM.getPath("/vault"));
        }
    }

    @BeforeAll
    static void setup() throws IOException {
        Files.createDirectories(FILE_SYSTEM.getPath("/stage"));

        expectStartupServiceCalls();
    }

    @AfterAll
    static void teardown() throws IOException {
        FILE_SYSTEM.close();
    }

    @BeforeEach
    public void setupEach() throws IOException {
        PathUtils.cleanDirectory(FILE_SYSTEM.getPath("/stage"));
        PathUtils.cleanDirectory(FILE_SYSTEM.getPath("/vault"));

        expectNonStartupServiceCalls();
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

        setupFakeFile("/vault/" + documentId + ".pdf", new byte[]{1, 2, 3, 4});

        mockMvc.perform(get("/document/" + documentId))
                .andExpect(status().isOk())
                .andExpect(content().bytes(new byte[]{1, 2, 3, 4}))
                .andExpect(header().stringValues("Content-Type", "application/pdf"));
    }

    @Test
    void testQueryDocumentWhenDocumentIsInVaultAndCompressedWithBrotli() throws Exception {
        final UUID documentId = UUID.randomUUID();

        documentEntityFactory.newDocumentEntity(
                DocumentCreationContext.builder()
                        .id(documentId)
                        .type(DocumentType.PDF)
                        .status(DocumentStatus.DOWNLOADED)
                        .checksum("ba8016bf8f01cfea414140de5dae2223b00361a396177a9cb410ff62f29915ad")
                        .compression(DocumentCompression.BROTLI)
                        .vault("default")
                        .fileSize(123)
                        .source("test-source")
                        .sourceLocationId(Optional.empty())
                        .build()
        );

        setupFakeFile("/vault/" + documentId + ".pdf.br", new byte[]{-117, 1, -128, 1, 2, 3, 4, 3});

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

        final Path fakeDocumentPath = setupFakeFile("/vault/" + documentId + ".pdf", new byte[]{1, 2, 3, 4});

        mockMvc.perform(delete("/document/" + documentId))
                .andExpect(status().isOk());

        assertThat(fakeDocumentPath)
                .doesNotExist();
        assertThat(documentEntityFactory.getDocumentEntity(documentId))
                .isEmpty();
    }

    @Test
    void testRecompressDocumentWhenDocumentNotFoundInDatabase() throws Exception {
        final UUID documentId = UUID.randomUUID();

        mockMvc.perform(
                        put("/document/" + documentId + "/recompress")
                                .contentType("application/json")
                                .content("{\"compression\": \"NONE\"}")
                )
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Document not found with id " + documentId + " or already removed!"));
    }

    @Test
    void testRecompressDocumentWhenDocumentIsUncompressedAndTargetIsGzip() throws Exception {
        final UUID documentId = UUID.randomUUID();

        documentEntityFactory.newDocumentEntity(
                DocumentCreationContext.builder()
                        .id(documentId)
                        .type(DocumentType.PDF)
                        .status(DocumentStatus.DOWNLOADED)
                        .checksum("ba8020bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad")
                        .compression(DocumentCompression.NONE)
                        .vault("default")
                        .fileSize(123)
                        .source("test-source")
                        .sourceLocationId(Optional.empty())
                        .build()
        );

        final Path fakeDocumentPath = setupFakeFile("/vault/" + documentId + ".pdf", new byte[]{1, 2, 3, 4});

        final Path resultDocumentPath = FILE_SYSTEM.getPath("/vault/" + documentId + ".pdf.gz");

        mockMvc.perform(
                        put("/document/" + documentId + "/recompress")
                                .contentType("application/json")
                                .content("{\"compression\": \"GZIP\"}")
                )
                .andExpect(status().isOk());

        verify(stageLocationFactory)
                .getLocation(uuidArgumentCaptor.capture());
        assertThat(uuidArgumentCaptor.getAllValues())
                .hasSize(1);
        assertThat(FILE_SYSTEM.getPath("/stage/" + uuidArgumentCaptor.getValue()))
                .doesNotExist();
        assertThat(FILE_SYSTEM.getPath("/stage/" + uuidArgumentCaptor.getValue() + ".gz"))
                .doesNotExist();

        assertThat(fakeDocumentPath)
                .doesNotExist();
        assertThat(resultDocumentPath)
                .binaryContent()
                .isEqualTo(new byte[]{31, -117, 8, 0, 0, 0, 0, 0, 0, -1, 99, 100, 98, 102, 1, 0, -51, -5, 60, -74, 4, 0, 0, 0});

        final Optional<DocumentEntity> documentInDatabase = documentEntityFactory.getDocumentEntity(documentId);

        assertThat(documentInDatabase)
                .isPresent()
                .hasValueSatisfying(databaseEntity -> {
                    assertThat(databaseEntity.getType())
                            .isEqualTo(DocumentType.PDF);
                    assertThat(databaseEntity.getStatus())
                            .isEqualTo(DocumentStatus.DOWNLOADED);
                    assertThat(databaseEntity.getCompression())
                            .isEqualTo(DocumentCompression.GZIP);
                    assertThat(databaseEntity.getChecksum())
                            .isEqualTo("ba8020bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
                    assertThat(databaseEntity.getFileSize())
                            .isEqualTo(123);
                    assertThat(databaseEntity.getSourceLocations())
                            .isEmpty();
                });
    }

    @Test
    void testRecompressDocumentWhenDocumentIsGzipAndTargetIsBrotli() throws Exception {
        final UUID documentId = UUID.randomUUID();

        documentEntityFactory.newDocumentEntity(
                DocumentCreationContext.builder()
                        .id(documentId)
                        .type(DocumentType.PDF)
                        .status(DocumentStatus.DOWNLOADED)
                        .checksum("ba8030bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad")
                        .compression(DocumentCompression.GZIP)
                        .vault("default")
                        .fileSize(123)
                        .source("test-source")
                        .sourceLocationId(Optional.empty())
                        .build()
        );

        final Path fakeDocumentPath = setupFakeFile("/vault/" + documentId + ".pdf.gz", new byte[]{
                31, -117, 8, 0, 0, 0, 0, 0, 0, -1, 99, 100, 98, 102, 1, 0, -51, -5, 60, -74, 4, 0, 0, 0});

        final Path resultDocumentPath = FILE_SYSTEM.getPath("/vault/" + documentId + ".pdf.br");

        mockMvc.perform(
                        put("/document/" + documentId + "/recompress")
                                .contentType("application/json")
                                .content("{\"compression\": \"BROTLI\"}")
                )
                .andExpect(status().isOk());

        verify(stageLocationFactory)
                .getLocation(uuidArgumentCaptor.capture());
        assertThat(uuidArgumentCaptor.getAllValues())
                .hasSize(1);
        assertThat(FILE_SYSTEM.getPath("/stage/" + uuidArgumentCaptor.getValue()))
                .doesNotExist();
        assertThat(FILE_SYSTEM.getPath("/stage/" + uuidArgumentCaptor.getValue() + ".br"))
                .doesNotExist();

        assertThat(fakeDocumentPath)
                .doesNotExist();
        assertThat(resultDocumentPath)
                .binaryContent()
                .isEqualTo(new byte[]{-117, 1, -128, 1, 2, 3, 4, 3});

        final Optional<DocumentEntity> documentInDatabase = documentEntityFactory.getDocumentEntity(documentId);

        assertThat(documentInDatabase)
                .isPresent()
                .hasValueSatisfying(databaseEntity -> {
                    assertThat(databaseEntity.getType())
                            .isEqualTo(DocumentType.PDF);
                    assertThat(databaseEntity.getStatus())
                            .isEqualTo(DocumentStatus.DOWNLOADED);
                    assertThat(databaseEntity.getCompression())
                            .isEqualTo(DocumentCompression.BROTLI);
                    assertThat(databaseEntity.getChecksum())
                            .isEqualTo("ba8030bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
                    assertThat(databaseEntity.getFileSize())
                            .isEqualTo(123);
                    assertThat(databaseEntity.getSourceLocations())
                            .isEmpty();
                });
    }

    @Test
    void testRecompressDocumentWhenDocumentIsGzipAndTargetIsNone() throws Exception {
        final UUID documentId = UUID.randomUUID();

        documentEntityFactory.newDocumentEntity(
                DocumentCreationContext.builder()
                        .id(documentId)
                        .type(DocumentType.PDF)
                        .status(DocumentStatus.DOWNLOADED)
                        .checksum("ba8040bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad")
                        .compression(DocumentCompression.GZIP)
                        .vault("default")
                        .fileSize(123)
                        .source("test-source")
                        .sourceLocationId(Optional.empty())
                        .build()
        );

        final Path fakeDocumentPath = setupFakeFile("/vault/" + documentId + ".pdf.gz", new byte[]{
                31, -117, 8, 0, 0, 0, 0, 0, 0, -1, 99, 100, 98, 102, 1, 0, -51, -5, 60, -74, 4, 0, 0, 0});

        final Path resultDocumentPath = FILE_SYSTEM.getPath("/vault/" + documentId + ".pdf");

        mockMvc.perform(
                        put("/document/" + documentId + "/recompress")
                                .contentType("application/json")
                                .content("{\"compression\": \"NONE\"}")
                )
                .andExpect(status().isOk());

        verify(stageLocationFactory)
                .getLocation(uuidArgumentCaptor.capture());
        assertThat(uuidArgumentCaptor.getAllValues())
                .hasSize(1);
        assertThat(FILE_SYSTEM.getPath("/stage/" + uuidArgumentCaptor.getValue()))
                .doesNotExist();

        assertThat(fakeDocumentPath)
                .doesNotExist();
        assertThat(resultDocumentPath)
                .binaryContent()
                .isEqualTo(new byte[]{1, 2, 3, 4});

        final Optional<DocumentEntity> documentInDatabase = documentEntityFactory.getDocumentEntity(documentId);

        assertThat(documentInDatabase)
                .isPresent()
                .hasValueSatisfying(databaseEntity -> {
                    assertThat(databaseEntity.getType())
                            .isEqualTo(DocumentType.PDF);
                    assertThat(databaseEntity.getStatus())
                            .isEqualTo(DocumentStatus.DOWNLOADED);
                    assertThat(databaseEntity.getCompression())
                            .isEqualTo(DocumentCompression.NONE);
                    assertThat(databaseEntity.getChecksum())
                            .isEqualTo("ba8040bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
                    assertThat(databaseEntity.getFileSize())
                            .isEqualTo(123);
                    assertThat(databaseEntity.getSourceLocations())
                            .isEmpty();
                });
    }

    @Test
    void testRecompressDocumentWhenDocumentIsInTheSameCompressionAsTarget() throws Exception {
        final UUID documentId = UUID.randomUUID();

        documentEntityFactory.newDocumentEntity(
                DocumentCreationContext.builder()
                        .id(documentId)
                        .type(DocumentType.PDF)
                        .status(DocumentStatus.DOWNLOADED)
                        .checksum("ba8050bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad")
                        .compression(DocumentCompression.GZIP)
                        .vault("default")
                        .fileSize(123)
                        .source("test-source")
                        .sourceLocationId(Optional.empty())
                        .build()
        );

        mockMvc.perform(
                        put("/document/" + documentId + "/recompress")
                                .contentType("application/json")
                                .content("{\"compression\": \"GZIP\"}")
                )
                .andExpect(status().isOk());
    }

    @Test
    void testDocumentExistsWhenDocumentIsInDifferentVault() throws Exception {
        final UUID documentId = UUID.randomUUID();

        documentEntityFactory.newDocumentEntity(
                DocumentCreationContext.builder()
                        .id(documentId)
                        .type(DocumentType.PDF)
                        .status(DocumentStatus.DOWNLOADED)
                        .checksum("ba7928bf8f01cfea414140de5dae2223b00361a396197a9cb420ff61f20015ad")
                        .compression(DocumentCompression.NONE)
                        .vault("not-this-one")
                        .fileSize(123)
                        .source("test-source")
                        .sourceLocationId(Optional.empty())
                        .build()
        );

        mockMvc.perform(get("/document/" + documentId + "/exists"))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Document with id " + documentId + " is available on a different vault!"));
    }

    @Test
    void testDocumentExistsWhenDocumentNotFoundInDatabase() throws Exception {
        final UUID documentId = UUID.randomUUID();

        mockMvc.perform(get("/document/" + documentId + "/exists"))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Document not found with id " + documentId + " or already removed!"));
    }

    @Test
    void testDocumentExistsWhenDocumentIsInTheVault() throws Exception {
        final UUID documentId = UUID.randomUUID();

        documentEntityFactory.newDocumentEntity(
                DocumentCreationContext.builder()
                        .id(documentId)
                        .type(DocumentType.PDF)
                        .status(DocumentStatus.DOWNLOADED)
                        .checksum("ba7928bf8f01cfea414140de5dae2223b00361a396197a9cb420ff61f20016ad")
                        .compression(DocumentCompression.NONE)
                        .vault("default")
                        .fileSize(123)
                        .source("test-source")
                        .sourceLocationId(Optional.empty())
                        .build()
        );

        setupFakeFile("/vault/" + documentId + ".pdf", new byte[]{1, 2, 3, 4});

        mockMvc.perform(get("/document/" + documentId + "/exists"))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"exists\":true}"));
    }

    @Test
    void testReplaceDocumentWhenDocumentIsInDifferentVault() throws Exception {
        final UUID documentId = UUID.randomUUID();

        documentEntityFactory.newDocumentEntity(
                DocumentCreationContext.builder()
                        .id(documentId)
                        .type(DocumentType.PDF)
                        .status(DocumentStatus.CORRUPT)
                        .checksum("ba7928bf8f01cfea414140de5dae2223b00361a396197a9cb420ff61f20016bd")
                        .compression(DocumentCompression.NONE)
                        .vault("not-this-one")
                        .fileSize(123)
                        .source("test-source")
                        .sourceLocationId(Optional.empty())
                        .build()
        );

        final MockMultipartFile mockMultipartFile = new MockMultipartFile("replacementFile", "dummy.pdf",
                "application/pdf", "Some dataset...".getBytes());

        mockMvc.perform(
                        multipart("/document/" + documentId + "/replace")
                                .file(mockMultipartFile)
                                .with(pp -> {
                                    pp.setMethod("PUT");
                                    return pp;
                                })
                )
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Document with id " + documentId + " is available on a different vault!"));
    }

    @Test
    void testReplaceDocumentWhenDocumentNotFoundInDatabase() throws Exception {
        final UUID documentId = UUID.randomUUID();

        final MockMultipartFile mockMultipartFile = new MockMultipartFile("replacementFile", "dummy.pdf",
                "application/pdf", "Some dataset...".getBytes());

        mockMvc.perform(
                        multipart("/document/" + documentId + "/replace")
                                .file(mockMultipartFile)
                                .with(pp -> {
                                    pp.setMethod("PUT");
                                    return pp;
                                })
                )
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Document not found with id " + documentId + " or already removed!"));
    }

    @Test
    void testReplaceDocumentWhenDocumentIsInVault() throws Exception {
        final UUID documentId = UUID.randomUUID();

        documentEntityFactory.newDocumentEntity(
                DocumentCreationContext.builder()
                        .id(documentId)
                        .type(DocumentType.PDF)
                        .status(DocumentStatus.DOWNLOADED)
                        .checksum("ba7928bf8f01cfea414140de5dae2223b00361a396197a9cb420ff61f20019ad")
                        .compression(DocumentCompression.NONE)
                        .vault("default")
                        .fileSize(123)
                        .source("test-source")
                        .sourceLocationId(Optional.empty())
                        .build()
        );

        final Path fakeDocumentPath = setupFakeFile("/vault/" + documentId + ".pdf", new byte[]{1, 2, 3, 4});

        final MockMultipartFile mockMultipartFile = new MockMultipartFile("replacementFile", "dummy.pdf",
                "application/pdf", new byte[]{4, 3, 2, 1});

        mockMvc.perform(
                        multipart("/document/" + documentId + "/replace")
                                .file(mockMultipartFile)
                                .with(pp -> {
                                    pp.setMethod("PUT");
                                    return pp;
                                })
                )
                .andExpect(status().isOk());

        assertThat(fakeDocumentPath)
                .binaryContent()
                .isEqualTo(new byte[]{4, 3, 2, 1});

        final Optional<DocumentEntity> documentInDatabase = documentEntityFactory.getDocumentEntity(documentId);

        assertThat(documentInDatabase)
                .isPresent()
                .hasValueSatisfying(databaseEntity -> {
                    assertThat(databaseEntity.getType())
                            .isEqualTo(DocumentType.PDF);
                    assertThat(databaseEntity.getStatus())
                            .isEqualTo(DocumentStatus.DOWNLOADED);
                    assertThat(databaseEntity.getCompression())
                            .isEqualTo(DocumentCompression.NONE);
                    assertThat(databaseEntity.getChecksum())
                            .isEqualTo("ba7928bf8f01cfea414140de5dae2223b00361a396197a9cb420ff61f20019ad");
                    assertThat(databaseEntity.getFileSize())
                            .isEqualTo(4);
                    assertThat(databaseEntity.getSourceLocations())
                            .isEmpty();
                });
    }

    @SneakyThrows
    private Path setupFakeFile(final String fileNameAndPath, final byte[] testFileContent) {
        final Path testFilePath = FILE_SYSTEM.getPath(fileNameAndPath);

        Files.copy(new ByteArrayInputStream(testFileContent), testFilePath);

        return testFilePath;
    }

    private static void expectStartupServiceCalls() {
        expectRegisterServiceCall(ApplicationType.VAULT_APPLICATION);

        expectQueryServiceCall(ApplicationType.DOCUMENT_DATABASE, "127.0.0.1", MONGO_CONTAINER.getFirstMappedPort());
        expectQueryServiceCall(ApplicationType.QUEUE_APPLICATION, "127.0.0.1", ARTEMIS_CONTAINER.getFirstMappedPort());
    }

    private void expectNonStartupServiceCalls() {
        expectRegisterServiceCall(ApplicationType.VAULT_APPLICATION);
    }
}
