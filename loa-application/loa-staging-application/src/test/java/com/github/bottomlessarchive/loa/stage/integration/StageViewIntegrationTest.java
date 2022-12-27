package com.github.bottomlessarchive.loa.stage.integration;

import com.github.bottomlessarchive.loa.application.domain.ApplicationType;
import com.github.bottomlessarchive.loa.file.FileManipulatorService;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

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

    @MockBean
    private FileManipulatorService fileManipulatorService;

    private FileSystem fileSystem;

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

        final Path resultFile = fileSystem.getPath("/stage/" + documentId);

        assertThat(resultFile)
                .exists()
                .binaryContent()
                .isEqualTo(content);
    }

    @SneakyThrows
    private Path createFakePath(final String fileNameAndPath) {
        final Path testFilePath = fileSystem.getPath(fileNameAndPath);

        Files.createDirectories(testFilePath.getParent());

        return testFilePath;
    }

    private static void expectStartupServiceCalls() {
        expectRegisterServiceCall(ApplicationType.STAGING_APPLICATION);
    }
}
