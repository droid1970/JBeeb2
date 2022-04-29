package com.jbeeb.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

public final class Runner {

    private static final long ADJUST_MASK = 0xFF;

    private final List<ClockListener> listeners = new ArrayList<>();
    private final long maxCycleCount;
    private long cycleCount;

    private final long frequencyHz;
    private final long initialDelayNanos;
    private long delayNanos;
    private long nextTickTime;

    public Runner(
            final long frequencyHz,
            final long maxCycleCount,
            final List<ClockListener> listeners
    ) {
        this.frequencyHz = frequencyHz;
        this.delayNanos = 1_000_000_000L / frequencyHz;
        this.initialDelayNanos = delayNanos;
        this.maxCycleCount = maxCycleCount;
        this.listeners.addAll(listeners);
    }

    public long getCycleCount() {
        return cycleCount;
    }

    public void run(final BooleanSupplier stopCondition) {
        final long startTime = System.nanoTime();
        this.nextTickTime = startTime + delayNanos;
        while (!stopCondition.getAsBoolean()) {
            nextCycle();
            tick();
            cycleCount++;
            if (cycleCount >= maxCycleCount) {
                return;
            }
            if ((cycleCount & ADJUST_MASK) == 0) {
                adjustDelay(System.nanoTime() - startTime);
            }
        }
    }

    private void adjustDelay(final long durationNanos) {
        final double secs = (double) durationNanos / 1_000_000_000L;
        final double cps = cycleCount / secs;
        final double delta = cps / frequencyHz;
        delayNanos = Math.min(initialDelayNanos, Math.max(10L, (long) (delayNanos * delta)));
    }

    private Runnable saveStateCallback;

    private void tick() {
        if (!listeners.isEmpty()) {
            for (ClockListener l : listeners) {
                l.tick();
            }
        }
    }

    private void nextCycle() {
        while (nextTickTime > 0L && (System.nanoTime() < nextTickTime)) {
            // Do nothing
        }
        nextTickTime += delayNanos;
    }
}
