package com.github.bottomlessarchive.loa.vault.integration;

import com.github.bottomlessarchive.loa.application.domain.ApplicationType;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.thomaskasene.wiremock.junit.WireMockStubs;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.UUID;

import static com.github.bottomlessarchive.loa.conductor.service.ConductorClientTestUtility.expectQueryServiceCall;
import static com.github.bottomlessarchive.loa.conductor.service.ConductorClientTestUtility.expectRegisterServiceCall;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@WireMockStubs
@Testcontainers
@SpringBootTest(
        properties = {
                "loa.conductor.port=2002",
                "loa.vault.archiving=false",
                "loa.vault.location.file.path=/vault/",
                "loa.vault.modification-enabled=false"
        }
)
@WireMockTest(httpPort = 2002)
@AutoConfigureMockMvc
class VaultViewModificationDisabledIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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

    @Test
    void testDeleteDocumentWhenDocumentModificationIsDisabled() throws Exception {
        final UUID documentId = UUID.randomUUID();

        mockMvc.perform(delete("/document/" + documentId))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Modification is disabled on this vault instance!"));
    }

    private static void expectStartupServiceCalls() {
        expectRegisterServiceCall(ApplicationType.VAULT_APPLICATION);

        expectQueryServiceCall(ApplicationType.DOCUMENT_DATABASE, "127.0.0.1", MONGO_CONTAINER.getFirstMappedPort());
        expectQueryServiceCall(ApplicationType.QUEUE_APPLICATION, "127.0.0.1", ARTEMIS_CONTAINER.getFirstMappedPort());
    }
}
