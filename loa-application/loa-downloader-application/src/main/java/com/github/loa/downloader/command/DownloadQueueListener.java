package com.github.loa.downloader.command;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Service
public class DownloadQueueListener {

    @JmsListener(destination = "loa.downloader")
    public void receive(final Message message) {
        System.out.println(message.getPayload());
    }
}
