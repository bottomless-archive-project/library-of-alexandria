package com.github.loa.queue.artemis.service.deserializer;

import com.github.loa.queue.service.domain.Queue;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
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
