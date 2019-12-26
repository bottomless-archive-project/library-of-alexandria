package com.github.loa.queue.artemis.service.serialize;

import com.github.loa.queue.artemis.configuration.QueueServerConfiguration;
import com.github.loa.queue.service.domain.Queue;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@ConditionalOnMissingBean(QueueServerConfiguration.class)
public class MessageSerializerProvider {

    private final Map<Queue, MessageSerializer<?>> messageSerializers;

    public MessageSerializerProvider(final List<MessageSerializer<?>> serializers) {
        messageSerializers = serializers.stream()
                .collect(Collectors.toMap(MessageSerializer::supports, Function.identity()));
    }

    public Optional<MessageSerializer<?>> getSerializer(final Queue queue) {
        return Optional.ofNullable(messageSerializers.get(queue));
    }
}
