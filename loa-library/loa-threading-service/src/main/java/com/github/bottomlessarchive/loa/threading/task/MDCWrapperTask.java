package com.github.bottomlessarchive.loa.threading.task;

import lombok.Builder;
import lombok.NonNull;
import org.slf4j.MDC;

import java.util.Map;

@Builder
public class MDCWrapperTask implements Runnable {

    @NonNull
    private final Map<String, String> mdcParameters;
    @NonNull
    private final Runnable task;

    @Override
    public void run() {
        mdcParameters.forEach(MDC::put);

        task.run();

        MDC.clear();
    }
}
