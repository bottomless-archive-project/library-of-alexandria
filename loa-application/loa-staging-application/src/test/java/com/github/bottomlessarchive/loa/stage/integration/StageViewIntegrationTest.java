package com.github.bottomlessarchive.loa.stage.integration;

import com.github.bottomlessarchive.loa.application.domain.ApplicationType;
import com.github.bottomlessarchive.loa.file.FileManipulatorService;
import com.github.bottomlessarchive.loa.stage.configuration.StagingConfigurationProperties;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import lombok.SneakyThrows;
import org.apache.commons.io.file.PathUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static com.github.bottomlessarchive.loa.conductor.service.ConductorClientTestUtility.expectRegisterServiceCall;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(
        properties = {
                "loa.conductor.port=2000",
                "loa.staging.location=/stage/"
        }
)
@WireMockTest(httpPort = 2000)
@AutoConfigureWebTestClient
class StageViewIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @SpyBean
    private FileManipulatorService fileManipulatorService;

    private static final FileSystem FILE_SYSTEM = Jimfs.newFileSystem(Configuration.unix());

    @TestConfiguration
    public static class ReplacementConfiguration {

        @Bean
        @Primary
        public StagingConfigurationProperties stagingConfigurationProperties() throws IOException {
            Files.createDirectories(FILE_SYSTEM.getPath("/stage"));

            return new StagingConfigurationProperties(FILE_SYSTEM.getPath("/stage"));
        }
    }

    @BeforeAll
    static void setup() {
        expectStartupServiceCalls();
    }

    @AfterAll
    static void teardown() throws IOException {
        FILE_SYSTEM.close();
    }

    @BeforeEach
    void setupEach() throws IOException {
        PathUtils.cleanDirectory(FILE_SYSTEM.getPath("/stage"));
    }

    @Test
    void testPersistDocument() {
        final UUID documentId = UUID.randomUUID();
        final byte[] content = {1, 2, 3, 4};

        final MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();

        when(fileManipulatorService.newFile("/stage/", documentId.toString()))
                .thenReturn(createFakePath("/stage/" + documentId));

        multipartBodyBuilder.part("file", new ByteArrayResource(content) {
                    @Override
                    public String getFilename() {
                        return "dummy.pdf";
                    }
                }, MediaType.APPLICATION_OCTET_STREAM)
                .contentType(MediaType.MULTIPART_FORM_DATA);

        webTestClient.post()
                .uri("/document/" + documentId)
                .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                .exchange()
                .expectStatus()
                .isOk();

        final Path resultFile = FILE_SYSTEM.getPath("/stage/" + documentId);

        assertThat(resultFile)
                .exists()
                .binaryContent()
                .isEqualTo(content);
    }

    @Test
    // See: https://stackoverflow.com/questions/74964892/how-to-do-integration-tests-for-endpoints-that-use-zerocopyhttpoutputmessage
    @Disabled
    void testServeDocument() {
        final UUID documentId = UUID.randomUUID();
        final byte[] content = {1, 2, 3, 4};

        final Path contentPath = setupFakeFile("/stage/" + documentId, content);
        when(fileManipulatorService.newFile("/stage/", documentId.toString()))
                .thenReturn(contentPath);

        final byte[] responseBody = webTestClient.get()
                .uri("/document/" + documentId)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .returnResult()
                .getResponseBody();

        assertThat(responseBody)
                .isEqualTo(content);
        assertThat(contentPath)
                .doesNotExist();
    }

    @Test
    void testDeleteDocument() {
        final UUID documentId = UUID.randomUUID();
        final byte[] content = {1, 2, 3, 4};

        final Path contentPath = setupFakeFile("/stage/" + documentId, content);
        when(fileManipulatorService.newFile("/stage/", documentId.toString()))
                .thenReturn(contentPath);

        webTestClient.delete()
                .uri("/document/" + documentId)
                .exchange()
                .expectStatus()
                .isOk();

        assertThat(contentPath)
                .doesNotExist();
    }

    @SneakyThrows
    private Path setupFakeFile(final String fileNameAndPath, final byte[] testFileContent) {
        final Path testFilePath = createFakePath(fileNameAndPath);

        Files.copy(new ByteArrayInputStream(testFileContent), testFilePath);

        return testFilePath;
    }

    @SneakyThrows
    private Path createFakePath(final String fileNameAndPath) {
        final Path testFilePath = FILE_SYSTEM.getPath(fileNameAndPath);

        Files.createDirectories(testFilePath.getParent());

        return testFilePath;
    }

    private static void expectStartupServiceCalls() {
        expectRegisterServiceCall(ApplicationType.STAGING_APPLICATION);
    }
}
