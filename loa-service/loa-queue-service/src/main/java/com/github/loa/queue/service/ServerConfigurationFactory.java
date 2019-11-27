package com.github.loa.queue.service;

import com.github.loa.queue.configuration.QueueConfigurationProperties;
import lombok.RequiredArgsConstructor;
import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyAcceptorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

@RequiredArgsConstructor
@ConditionalOnClass(EmbeddedActiveMQ.class)
@org.springframework.context.annotation.Configuration
public class ServerConfigurationFactory {

    private final QueueConfigurationProperties queueConfigurationProperties;

    @Bean
    public EmbeddedActiveMQ embeddedActiveMQ(final Configuration configuration) {
        final EmbeddedActiveMQ embeddedActiveMQ = new EmbeddedActiveMQ();

        embeddedActiveMQ.setConfiguration(configuration);

        return embeddedActiveMQ;
    }

    @Bean
    protected Configuration queueServerConfiguration() {
        final Configuration configuration = new ConfigurationImpl();

        configuration.setSecurityEnabled(false);
        configuration.addConnectorConfiguration("netty-connector", new TransportConfiguration(
                NettyConnectorFactory.class.getName()));
        configuration.addAcceptorConfiguration(new TransportConfiguration(NettyAcceptorFactory.class.getName()));
        configuration.setJournalDirectory(queueConfigurationProperties.getDataDirectory() + "/journal");
        configuration.setBindingsDirectory(queueConfigurationProperties.getDataDirectory() + "/bindings");
        configuration.setLargeMessagesDirectory(queueConfigurationProperties.getDataDirectory() + "/largemessages");

        return configuration;
    }
}
