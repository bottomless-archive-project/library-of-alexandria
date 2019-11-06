package com.github.loa.downloader.command;

import com.github.loa.document.repository.domain.DocumentLocationDatabaseEntity;
import com.github.loa.document.service.location.factory.DocumentLocationEntityFactory;
import com.github.loa.document.service.location.id.factory.DocumentLocationIdFactory;
import com.github.loa.downloader.download.service.document.DocumentDownloader;
import com.github.loa.source.domain.DocumentSourceItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DownloadQueueListener {

    private final DocumentDownloader documentDownloader;
    private final DocumentLocationIdFactory documentLocationIdFactory;
    private final DocumentLocationEntityFactory documentLocationEntityFactory;

    @JmsListener(destination = "loa.downloader", concurrency = "10-10")
    public void receive(final Message<DocumentSourceItem> message) {
        final DocumentSourceItem documentSourceItem = message.getPayload();

        final String documentId = documentLocationIdFactory.newDocumentId(documentSourceItem.getDocumentLocation());

        //TODO: Don't leak database details to here!
        final DocumentLocationDatabaseEntity documentLocationDatabaseEntity = new DocumentLocationDatabaseEntity();
        documentLocationDatabaseEntity.setId(documentId);
        documentLocationDatabaseEntity.setDownloaderVersion(1);
        documentLocationDatabaseEntity.setSource(documentSourceItem.getSourceName());
        documentLocationDatabaseEntity.setUrl(documentSourceItem.getDocumentLocation().toString());

        documentLocationEntityFactory.isDocumentLocationExistsOrCreate(documentLocationDatabaseEntity)
                .filter(exists -> !exists)
                .map(exists -> documentSourceItem)
                .flatMap(documentDownloader::downloadDocument)
                .block();
    }
}
