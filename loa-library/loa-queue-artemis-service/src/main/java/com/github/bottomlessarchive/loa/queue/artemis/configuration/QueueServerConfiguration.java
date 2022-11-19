package com.github.bottomlessarchive.loa.queue.artemis.configuration;

import com.github.bottomlessarchive.loa.queue.configuration.QueueServerConfigurationProperties;
import lombok.RequiredArgsConstructor;
import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyAcceptorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.TransportConstants;
import org.apache.activemq.artemis.core.server.JournalType;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

import java.util.Map;

@RequiredArgsConstructor
@ConditionalOnClass(EmbeddedActiveMQ.class)
@org.springframework.context.annotation.Configuration
public class QueueServerConfiguration {

    private static final String BIND_ALL_IP_ADDRESS = "0.0.0.0";

    private final QueueServerConfigurationProperties queueServerConfigurationProperties;

    @Bean
    public EmbeddedActiveMQ embeddedActiveMQ(final Configuration configuration) {
        final EmbeddedActiveMQ embeddedActiveMQ = new EmbeddedActiveMQ();

        embeddedActiveMQ.setConfiguration(configuration);

        return embeddedActiveMQ;
    }

    @Bean
    protected Configuration artemisServerConfiguration() {
        final Configuration configuration = new ConfigurationImpl();

        configuration.setSecurityEnabled(false);
        configuration.addConnectorConfiguration("netty-connector", new TransportConfiguration(
                NettyConnectorFactory.class.getName()));
        configuration.addAcceptorConfiguration(new TransportConfiguration(NettyAcceptorFactory.class.getName(),
                        Map.of(
                                TransportConstants.HOST_PROP_NAME, BIND_ALL_IP_ADDRESS,
                                TransportConstants.PORT_PROP_NAME, queueServerConfigurationProperties.port()
                        )
                )
        );
        configuration.setJournalType(JournalType.MAPPED);
        configuration.setJournalSyncTransactional(false);
        configuration.setJournalSyncNonTransactional(false);
        configuration.setMaxDiskUsage(-1);
        configuration.setPagingDirectory(queueServerConfigurationProperties.dataDirectory() + "/paging");
        configuration.setJournalDirectory(queueServerConfigurationProperties.dataDirectory() + "/journal");
        configuration.setBindingsDirectory(queueServerConfigurationProperties.dataDirectory() + "/bindings");
        configuration.setLargeMessagesDirectory(queueServerConfigurationProperties.dataDirectory() + "/largemessages");

        return configuration;
    }
}
