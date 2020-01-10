package com.github.loa.generator.configuration;

import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeneratorConfiguration {

    @Bean
    public ClientSession clientSession(final ClientSessionFactory clientSessionFactory) throws ActiveMQException {
        return clientSessionFactory.createSession();
    }
}
