package com.github.bottomlessarchive.loa.downloader.integration;

import com.github.bottomlessarchive.loa.application.domain.ApplicationType;
import com.github.bottomlessarchive.loa.downloader.service.source.queue.QueueMessageHandler;
import com.github.bottomlessarchive.loa.stage.service.StageLocationFactory;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.thomaskasene.wiremock.junit.WireMockStubs;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import wiremock.org.apache.commons.io.file.PathUtils;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.time.Duration;

import static com.github.bottomlessarchive.loa.conductor.service.ConductorClientTestUtility.expectQueryServiceCall;
import static com.github.bottomlessarchive.loa.conductor.service.ConductorClientTestUtility.expectRegisterServiceCall;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@Disabled // TODO: This test always ends up with an outofmemory error, further investigation is needed!
@Slf4j
@WireMockStubs
@Testcontainers
@SpringBootTest(
        properties = {
                "loa.conductor.port=2002",
                "loa.stage.location=./build/"
        }
)
@WireMockTest(httpPort = 2002)
class QueueSourceIntegrationTest {

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

    private static final FileSystem FILE_SYSTEM = Jimfs.newFileSystem(Configuration.unix());

    @SpyBean
    private QueueMessageHandler queueMessageHandler;

    @TestConfiguration
    public static class ReplacementConfiguration {

        @Bean
        @Primary
        public StageLocationFactory stageLocationFactory() throws IOException {
            Files.createDirectories(FILE_SYSTEM.getPath("/stage"));

            return new StageLocationFactory(FILE_SYSTEM.getPath("/stage"));
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

        expectNonStartupServiceCalls();
    }

    @Test
    void testNoMessage() throws InterruptedException {
        Thread.sleep(Duration.ofSeconds(1));

        verify(queueMessageHandler, never())
                .handleMessage(any());
    }

    private static void expectStartupServiceCalls() {
        expectRegisterServiceCall(ApplicationType.DOWNLOADER_APPLICATION);

        expectQueryServiceCall(ApplicationType.DOCUMENT_DATABASE, "127.0.0.1", MONGO_CONTAINER.getFirstMappedPort());
        expectQueryServiceCall(ApplicationType.QUEUE_APPLICATION, "127.0.0.1", ARTEMIS_CONTAINER.getFirstMappedPort());
    }

    private void expectNonStartupServiceCalls() {
        expectRegisterServiceCall(ApplicationType.DOWNLOADER_APPLICATION);
    }
}
