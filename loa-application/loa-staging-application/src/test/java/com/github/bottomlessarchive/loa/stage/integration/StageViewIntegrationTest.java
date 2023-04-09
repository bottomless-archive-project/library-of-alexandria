package com.github.bottomlessarchive.loa.stage.integration;

import com.github.bottomlessarchive.loa.application.domain.ApplicationType;
import com.github.bottomlessarchive.loa.stage.configuration.StagingConfigurationProperties;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import lombok.SneakyThrows;
import org.apache.commons.io.file.PathUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
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

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "loa.conductor.port=2000",
                "loa.staging.location=/stage/",
                "loa.conductor.application-port=8099"
        }
)
@WireMockTest(httpPort = 2000)
class StageViewIntegrationTest {

    @LocalServerPort
    private Integer serverPort;

    private WebTestClient webTestClient;

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
        expectRegisterServiceCall(ApplicationType.STAGING_APPLICATION);
    }

    @AfterAll
    static void teardown() throws IOException {
        FILE_SYSTEM.close();
    }

    @BeforeEach
    void setupEach() throws IOException {
        PathUtils.cleanDirectory(FILE_SYSTEM.getPath("/stage"));

        // It is being done like this because the "normal/default" WebTestClient doesn't support the ZeroCopy message in the serve document
        // test case.
        webTestClient = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:" + serverPort)
                .build();
    }

    @Test
    void testPersistDocument() {
        final UUID documentId = UUID.randomUUID();
        final byte[] content = {1, 2, 3, 4};

        final MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();

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
    void testServeDocument() {
        final UUID documentId = UUID.randomUUID();
        final byte[] content = {1, 2, 3, 4};

        final Path contentPath = setupFakeFile("/stage/" + documentId, content);

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
        final Path testFilePath = FILE_SYSTEM.getPath(fileNameAndPath);

        Files.copy(new ByteArrayInputStream(testFileContent), testFilePath);

        return testFilePath;
    }
}
