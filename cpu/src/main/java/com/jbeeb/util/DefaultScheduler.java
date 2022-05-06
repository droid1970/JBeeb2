package com.jbeeb.util;

import com.jbeeb.clock.ClockListener;

import java.util.ArrayList;
import java.util.List;

public class DefaultScheduler implements Scheduler, ClockListener {

    private long cycleCount;

    private final List<ScheduleEntry> entries = new ArrayList<>();
    private final List<ScheduleEntry> entriesToRun = new ArrayList<>();
    private final List<ScheduleEntry> staleEntries = new ArrayList<>();

    private static final class ScheduleEntry {
        long counter;
        final ScheduledTask task;

        ScheduleEntry(long counter, ScheduledTask task) {
            this.counter = counter;
            this.task = task;
        }

        boolean tick() {
            if (--counter == 0) {
                return true;
            }
            return false;
        }
    }

    @Override
    public void tick() {
        if (!entries.isEmpty()) {
            entriesToRun.clear();
            boolean run = false;
            for (ScheduleEntry e : entries) {
                if (e.tick()) {
                    entriesToRun.add(e);
                    run = true;
                }
            }
            if (run) {
                for (ScheduleEntry e : entriesToRun) {
                    e.task.run();
                    staleEntries.add(e);
                }
                removeStaleEntries();
            }
        }
        cycleCount++;
    }

    private void removeStaleEntries() {
        if (!staleEntries.isEmpty()) {
            for (ScheduleEntry e : staleEntries) {
                entries.remove(e);
            }
        }
        staleEntries.clear();
    }

    @Override
    public ScheduledTask newTask(Runnable runnable) {
        return new ScheduledTask(this, runnable);
    }

    @Override
    public void schedule(ScheduledTask task, long delay) {
        entries.add(new ScheduleEntry(delay, task));
    }

    @Override
    public void unschedule(ScheduledTask task) {
        boolean removed = false;
        for (ScheduleEntry e : entries) {
            if (e.task == task) {
                staleEntries.add(e);
                removed = true;
            }
        }
        if (removed) {
            removeStaleEntries();
        }
    }
}
