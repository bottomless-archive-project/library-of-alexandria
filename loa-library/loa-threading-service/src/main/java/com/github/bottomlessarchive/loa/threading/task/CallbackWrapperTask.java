package com.github.bottomlessarchive.loa.threading.task;

import lombok.Builder;
import lombok.NonNull;

@Builder
public class CallbackWrapperTask implements Runnable {

    @NonNull
    private final Runnable task;
    private final Runnable callback;

    @Override
    public void run() {
        task.run();

        if (callback != null) {
            callback.run();
        }
    }
}
