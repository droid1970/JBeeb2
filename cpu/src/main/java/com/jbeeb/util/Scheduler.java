package com.jbeeb.util;

import com.jbeeb.clock.ClockListener;

public interface Scheduler extends ClockListener {
    ScheduledTask newTask(final Runnable runnable);
    void schedule(ScheduledTask task, final long delay);
    void unschedule(ScheduledTask task);
}
