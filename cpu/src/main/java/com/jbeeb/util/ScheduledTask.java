package com.jbeeb.util;
;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public class ScheduledTask {

    private static final AtomicLong NEXT_ID = new AtomicLong(1L);

    private final long id;
    private final Scheduler scheduler;
    private final Runnable runnable;

    public ScheduledTask(final Scheduler scheduler, final Runnable runnable) {
        this.id = NEXT_ID.getAndIncrement();
        this.scheduler = Objects.requireNonNull(scheduler);
        this.runnable = Objects.requireNonNull(runnable);
    }

    public long getId() {
        return id;
    }

    public void schedule(final long delay) {
        scheduler.schedule(this, delay);
    }

    public void reschedule(final long delay) {
        cancel();
        schedule(delay);
    }

    public void cancel() {
        scheduler.unschedule(this);
    }

    public void run() {
        runnable.run();
    }
}
