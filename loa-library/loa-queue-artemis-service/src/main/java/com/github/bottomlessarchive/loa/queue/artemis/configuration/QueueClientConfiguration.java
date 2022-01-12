package com.github.bottomlessarchive.loa.queue.artemis.configuration;

import com.github.bottomlessarchive.loa.application.domain.ApplicationType;
import com.github.bottomlessarchive.loa.conductor.service.client.ConductorClient;
import com.github.bottomlessarchive.loa.conductor.service.domain.ServiceInstanceEntity;
import com.github.bottomlessarchive.loa.queue.service.domain.QueueException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.api.core.client.ActiveMQClient;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.apache.activemq.artemis.api.core.client.ServerLocator;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.TransportConstants;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnMissingClass("org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ")
public class QueueClientConfiguration {

    private final ConductorClient conductorClient;

    /*
     * We create a default session that's necessary to create messages in the serializers/deserializers etc.
     */
    @Bean(destroyMethod = "stop")
    public ClientSession clientSession(final ClientSessionFactory clientSessionFactory) {
        try {
            return clientSessionFactory.createSession();
        } catch (final Exception e) {
            throw new QueueException("Unable to create ClientSession!", e);
        }
    }

    @Bean
    public ClientSessionFactory clientSessionFactory(final ServerLocator serverLocator) {
        try {
            return serverLocator.createSessionFactory();
        } catch (final Exception e) {
            throw new QueueException("Unable to create ClientSessionFactory!", e);
        }
    }

    @Bean
    protected ServerLocator serverLocator(final TransportConfiguration transportConfiguration) {
        return ActiveMQClient.createServerLocatorWithoutHA(transportConfiguration)
                .setReconnectAttempts(-1)
                .setConfirmationWindowSize(134217728)
                .setPreAcknowledge(true);
    }

    @Bean
    protected TransportConfiguration serverTransportConfiguration() throws InterruptedException {
        ServiceInstanceEntity serviceInstanceEntity = null;

        while (serviceInstanceEntity == null) {
            final Optional<ServiceInstanceEntity> serviceInstanceEntityResponse = conductorClient.getInstance(
                    ApplicationType.QUEUE_APPLICATION).blockOptional();

            if (serviceInstanceEntityResponse.isPresent()) {
                serviceInstanceEntity = serviceInstanceEntityResponse.get();
            } else {
                log.info("Failed to acquire connection to a Queue Application. Will retry in 5 seconds.");

                Thread.sleep(5000L);
            }
        }

        log.info("Connecting to Queue Application at location: {}, port: {}.", serviceInstanceEntity.getLocation(),
                serviceInstanceEntity.getPort());

        return new TransportConfiguration(NettyConnectorFactory.class.getName(),
                Map.of(
                        TransportConstants.HOST_PROP_NAME, serviceInstanceEntity.getLocation(),
                        TransportConstants.PORT_PROP_NAME, serviceInstanceEntity.getPort()
                )
        );
    }
}
