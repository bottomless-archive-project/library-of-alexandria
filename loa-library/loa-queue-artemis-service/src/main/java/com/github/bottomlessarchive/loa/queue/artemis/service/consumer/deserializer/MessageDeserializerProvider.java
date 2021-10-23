package com.github.bottomlessarchive.loa.queue.artemis.service.consumer.deserializer;

import com.github.bottomlessarchive.loa.queue.artemis.configuration.QueueServerConfiguration;
import com.github.bottomlessarchive.loa.queue.service.domain.Queue;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@ConditionalOnMissingBean(QueueServerConfiguration.class)
public class MessageDeserializerProvider {

    private final Map<Queue, MessageDeserializer<?>> messageDeserializers;

    public MessageDeserializerProvider(final List<MessageDeserializer<?>> deserializers) {
        messageDeserializers = deserializers.stream()
                .collect(Collectors.toMap(MessageDeserializer::supports, Function.identity()));
    }

    public Optional<MessageDeserializer<?>> getDeserializer(final Queue queue) {
        return Optional.ofNullable(messageDeserializers.get(queue));
    }
}
