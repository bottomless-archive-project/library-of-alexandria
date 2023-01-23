package com.github.bottomlessarchive.loa.threading.task;

import io.micrometer.core.instrument.Counter;
import lombok.Builder;
import lombok.NonNull;

@Builder
public class CounterIncrementingWrapperTask implements Runnable {

    @NonNull
    private final Counter counter;
    @NonNull
    private final Runnable task;

    @Override
    public void run() {
        counter.increment();

        task.run();
    }
}
