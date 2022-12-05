package com.github.bottomlessarchive.loa.vault.integration;

import com.github.bottomlessarchive.loa.application.domain.ApplicationType;
import com.github.bottomlessarchive.loa.compression.domain.DocumentCompression;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentStatus;
import com.github.bottomlessarchive.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.bottomlessarchive.loa.queue.service.QueueManipulator;
import com.github.bottomlessarchive.loa.queue.service.domain.Queue;
import com.github.bottomlessarchive.loa.queue.service.domain.message.DocumentArchivingMessage;
import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.UUID;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@Testcontainers
@SpringBootTest(
        properties = {
                "loa.conductor.port=2000",
                "loa.vault.location.file.path=./build/"
        }
)
@WireMockTest(httpPort = 2000)
class VaultIntegrationTest {

    @Autowired
    private QueueManipulator queueManipulator;

    @Autowired
    private DocumentEntityFactory documentEntityFactory;

    @Container
    @SuppressWarnings("unchecked")
    public static final GenericContainer ARTEMIS_CONTAINER = new GenericContainer("vromero/activemq-artemis")
            .withExposedPorts(61616)
            .withStartupTimeout(Duration.ofMinutes(5))
            .withLogConsumer(new Slf4jLogConsumer(log).withPrefix("AMQ-LOG"))
            .withEnv("DISABLE_SECURITY", "true")
            .withEnv("BROKER_CONFIG_GLOBAL_MAX_SIZE", "50000")
            .withEnv("BROKER_CONFIG_MAX_SIZE_BYTES", "50000")
            .withEnv("BROKER_CONFIG_MAX_DISK_USAGE", "100");

    @Container
    public static final MongoDBContainer MONGO_CONTAINER = new MongoDBContainer("mongo:6.0.1")
            .withStartupTimeout(Duration.ofMinutes(5))
            .withLogConsumer(new Slf4jLogConsumer(log).withPrefix("MONGO-LOG"));

    @BeforeAll
    static void setup() {
        // These things are being queried at startup
        expectRegisterServiceCall(ApplicationType.VAULT_APPLICATION);

        expectQueryServiceCall(ApplicationType.DOCUMENT_DATABASE, "127.0.0.1", MONGO_CONTAINER.getFirstMappedPort());
        expectQueryServiceCall(ApplicationType.QUEUE_APPLICATION, "127.0.0.1", ARTEMIS_CONTAINER.getFirstMappedPort());
    }

    @Test
    void testNewDocumentArchival() throws InterruptedException {
        expectNonStartupServiceCalls();

        final UUID documentId = UUID.randomUUID();

        expectStagingGetDocumentCall(documentId, new byte[]{1, 2, 3, 4, 5});

        queueManipulator.sendMessage(Queue.DOCUMENT_ARCHIVING_QUEUE,
                DocumentArchivingMessage.builder()
                        .id(documentId.toString())
                        .fromBeacon(false)
                        .type(DocumentType.PDF.name())
                        .checksum("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad")
                        .compression(DocumentCompression.NONE.name())
                        .contentLength(555)
                        .originalContentLength(444)
                        .source("test-source")
                        .sourceLocationId(Optional.empty())
                        .build()
        );

        Thread.sleep(5000); // So the message is actually processed

        final Optional<DocumentEntity> documentEntity = documentEntityFactory.getDocumentEntity(documentId);

        assertThat(documentEntity)
                .isPresent()
                .hasValueSatisfying(document -> {
                    assertThat(document.getId())
                            .isEqualTo(documentId);
                    assertThat(document.getVault())
                            .isEqualTo("default");
                    assertThat(document.getType())
                            .isEqualTo(DocumentType.PDF);
                    assertThat(document.getStatus())
                            .isEqualTo(DocumentStatus.DOWNLOADED);
                    assertThat(document.getDownloadDate())
                            .isBetween(Instant.now().minus(1, ChronoUnit.MINUTES), Instant.now());
                    assertThat(document.getChecksum())
                            .isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
                    assertThat(document.getFileSize())
                            .isEqualTo(444);
                    assertThat(document.getCompression())
                            .isEqualTo(DocumentCompression.NONE);
                    assertThat(document.getSource())
                            .isEqualTo("test-source");
                    assertThat(document.getBeacon())
                            .isEmpty();
                    assertThat(document.getSourceLocations())
                            .isEmpty();
                });

        assertThat(new File("./build/" + documentId + ".pdf"))
                .exists()
                .binaryContent()
                .isEqualTo(new byte[]{1, 2, 3, 4, 5});
    }

    private void expectNonStartupServiceCalls() {
        expectQueryServiceCall(ApplicationType.STAGING_APPLICATION, "127.0.0.1", 2000);
    }

    private static void expectRegisterServiceCall(final ApplicationType applicationType) {
        stubFor(
                post("/service/" + applicationType.name().replaceAll("_", "-").toLowerCase(Locale.ENGLISH))
                        .willReturn(
                                ok()
                                        .withHeader("Content-Type", "application/json")
                                        .withBody("{\"id\":\"" + UUID.randomUUID() + "\"}")
                        )
        );
    }

    private static void expectQueryServiceCall(final ApplicationType applicationType, final String host, final int port) {
        stubFor(
                get("/service/" + applicationType.name().replaceAll("_", "-").toLowerCase(Locale.ENGLISH))
                        .willReturn(
                                ok()
                                        .withHeader("Content-Type", "application/json")
                                        .withBody("{\"applicationType\":\"" + applicationType + "\",\"instances\":[{\"id\":"
                                                + "\"1e0d6192-5a47-4dfa-b8d3-ba64f01de11a\",\"location\":\"" + host + "\",\"port\":"
                                                + port + ",\"lastHeartbeat\":\"2022-12-03T18:23:40.319067100Z\",\"properties\":[{"
                                                + "\"name\":\"archivingQueueCount\",\"value\":\"-1\"},{\"name\":\"locationQueueCount\","
                                                + "\"value\":\"-1\"}]}]}")
                        )
        );
    }

    private void expectStagingGetDocumentCall(final UUID documentId, final byte[] documentContent) {
        stubFor(
                get("/document/" + documentId)
                        .willReturn(
                                ok()
                                        .withHeader("Content-Type", "application/octet-stream")
                                        .withBody(documentContent)
                        )
        );
    }
}
