package com.jbeeb.util;

public interface Scheduler {
    void tick();
    ScheduledTask newTask(final Runnable runnable);
    void schedule(ScheduledTask task, final long delay);
    void unschedule(ScheduledTask task);
}
