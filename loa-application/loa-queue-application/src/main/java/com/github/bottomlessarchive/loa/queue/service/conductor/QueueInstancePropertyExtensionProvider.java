package com.github.bottomlessarchive.loa.queue.service.conductor;

import com.github.bottomlessarchive.loa.conductor.service.client.extension.InstancePropertyExtensionProvider;
import com.github.bottomlessarchive.loa.conductor.service.client.extension.domain.InstanceExtensionContext;
import com.github.bottomlessarchive.loa.queue.service.domain.Queue;
import lombok.RequiredArgsConstructor;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QueueInstancePropertyExtensionProvider implements InstancePropertyExtensionProvider {

    private final EmbeddedActiveMQ embeddedActiveMQ;

    @Override
    public void extendInstanceWithProperty(final InstanceExtensionContext instanceExtensionContext) {
        instanceExtensionContext.setProperty("locationQueueCount", getMessageCount(Queue.DOCUMENT_LOCATION_QUEUE));
        instanceExtensionContext.setProperty("archivingQueueCount", getMessageCount(Queue.DOCUMENT_ARCHIVING_QUEUE));
    }

    private String getMessageCount(final Queue queue) {
        return String.valueOf(
                Optional.ofNullable(embeddedActiveMQ.getActiveMQServer())
                        .flatMap(activeMQServer -> Optional.ofNullable(activeMQServer.locateQueue(queue.getName())))
                        .map(org.apache.activemq.artemis.core.server.Queue::getMessageCount)
                        .orElse(-1L)
        );
    }
}
