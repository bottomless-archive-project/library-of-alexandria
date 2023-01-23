package com.github.bottomlessarchive.loa.threading.thread;

import org.springframework.stereotype.Service;

@Service
public class ThreadManipulator {

    public void runInNewThread(final Runnable runnable) {
        new Thread(runnable).start();
    }
}
