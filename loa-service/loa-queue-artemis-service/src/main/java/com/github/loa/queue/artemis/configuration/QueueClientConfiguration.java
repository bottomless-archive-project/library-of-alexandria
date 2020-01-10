package com.github.loa.queue.artemis.configuration;

import com.github.loa.queue.configuration.QueueConfigurationProperties;
import lombok.RequiredArgsConstructor;
import org.apache.activemq.artemis.api.core.ActiveMQException;
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

@Configuration
@RequiredArgsConstructor
@ConditionalOnMissingClass("org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ")
public class QueueClientConfiguration {

    private final QueueConfigurationProperties queueConfigurationProperties;

    /*
     * We create a default session that's necessary to create messages in the serializers/deserializers etc.
     */
    @Bean(destroyMethod = "stop")
    public ClientSession clientSession(final ClientSessionFactory clientSessionFactory) throws ActiveMQException {
        return clientSessionFactory.createSession();
    }

    @Bean
    public ClientSessionFactory clientSessionFactory(final ServerLocator serverLocator) throws Exception {
        return serverLocator.createSessionFactory();
    }

    @Bean
    protected ServerLocator serverLocator(final TransportConfiguration transportConfiguration) {
        return ActiveMQClient.createServerLocatorWithoutHA(transportConfiguration)
                .setPreAcknowledge(true);
    }

    @Bean
    protected TransportConfiguration serverTransportConfiguration() {
        return new TransportConfiguration(NettyConnectorFactory.class.getName(),
                Map.of(
                        TransportConstants.HOST_PROP_NAME, queueConfigurationProperties.getHost(),
                        TransportConstants.PORT_PROP_NAME, queueConfigurationProperties.getPort()
                )
        );
    }
}
