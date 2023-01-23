package com.github.bottomlessarchive.loa.threading.executor;

import lombok.SneakyThrows;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * This {@link Executor} is allowing a certain level of parallelism (tasks running at the same time) with a queue that blocks the producers
 * after certain amount of unprocessed items. As soon as the queue's length goes under the unprocessed item limit (so the tasks are
 * consumed by the executor), the blocked producers will be unblocked.
 */
public class BlockingExecutor implements Executor {

    private final Semaphore semaphore;
    private final ExecutorService executorService;

    public BlockingExecutor(final int threadCount, final int queueLength) {
        semaphore = new Semaphore(queueLength);
        executorService = Executors.newFixedThreadPool(threadCount);
    }

    @Override
    @SneakyThrows
    public void execute(final Runnable command) {
        semaphore.acquire();

        executorService.execute(() -> {
            try {
                command.run();
            } finally {
                semaphore.release();
            }
        });
    }
}
