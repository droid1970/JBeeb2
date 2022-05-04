package com.jbeeb.util;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class Task {

    private final ScheduledExecutorService executor;
    private final Runnable runnable;

    private ScheduledFuture<?> future;

    public Task(ScheduledExecutorService executor, Runnable runnable) {
        this.executor = executor;
        this.runnable = runnable;
    }

    public void cancel() {
        if (this.future != null) {
            this.future.cancel(true);
        }
    }

    public void reschedule(final long nanos) {
        cancel();
        schedule(nanos * 500);
    }

    public void schedule(final long nanos) {
        this.future = executor.schedule(runnable, nanos, TimeUnit.NANOSECONDS);
    }
}
