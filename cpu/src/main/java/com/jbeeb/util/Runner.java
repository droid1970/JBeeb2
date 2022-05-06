package com.jbeeb.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;

public final class Runner {

    private static final long RESET_CYCLES = 2_000_000L;
    private static final long ADJUST_MASK = 0xFF;

    private final SystemStatus systemStatus;
    private final ClockListener[] listeners;
    private final long maxCycleCount;
    private long cycleCount;
    private long cycleCountSinceReset;

    private final long frequencyHz;
    private final long initialDelayNanos;
    private long delayNanos;
    private long nextTickTime;

    public Runner(
            final SystemStatus systemStatus,
            final long frequencyHz,
            final long maxCycleCount,
            final List<ClockListener> listeners
    ) {
        this.systemStatus = Objects.requireNonNull(systemStatus);
        this.frequencyHz = frequencyHz;
        this.delayNanos = 1_000_000_000L / frequencyHz;
        this.initialDelayNanos = delayNanos;
        this.maxCycleCount = maxCycleCount;
        this.listeners = new ClockListener[listeners.size()];
        for (int i = 0; i < listeners.size(); i++) {
            this.listeners[i] = listeners.get(i);
        }
    }

    public long getCycleCount() {
        return cycleCount;
    }

    public void run(final BooleanSupplier stopCondition) {
        long startTime = System.nanoTime();
        this.nextTickTime = startTime + delayNanos;
        while (!stopCondition.getAsBoolean()) {
            nextCycle();
            tick();
            cycleCount++;
            cycleCountSinceReset++;
            if (cycleCount >= maxCycleCount) {
                return;
            }
            if ((cycleCountSinceReset & ADJUST_MASK) == 0) {
                adjustDelay(System.nanoTime() - startTime);
            }
            if (cycleCountSinceReset >= RESET_CYCLES) {
                delayNanos = initialDelayNanos;
                final long now = System.nanoTime();
                updateSystemStatus(now - startTime);
                startTime = now;
                cycleCountSinceReset = 0L;
                nextTickTime = startTime + delayNanos;
            }
        }
    }

    private static final NumberFormat FMT = new DecimalFormat("0.00");
    private void updateSystemStatus(final long duration) {
        final double seconds = (double) duration / 1_000_000_000L;
        final double cyclesPerSecond = cycleCountSinceReset / seconds / 1000000.0;
        systemStatus.putString(SystemStatus.KEY_MILLION_CYCLES_PER_SECOND, FMT.format(cyclesPerSecond));
        systemStatus.putLong(SystemStatus.KEY_TOTAL_CYCLES, cycleCount);
        systemStatus.putString(SystemStatus.KEY_UP_TIME, FMT.format(seconds));
    }

    private void adjustDelay(final long durationNanos) {
        final double secs = (double) durationNanos / 1_000_000_000L;
        final double cps = cycleCountSinceReset / secs;
        final double delta = cps / frequencyHz;
        delayNanos = Math.min(initialDelayNanos, Math.max(10L, (long) (delayNanos * delta)));
    }

    private Runnable saveStateCallback;

    private void tick() {
        for (ClockListener l : listeners) {
            l.tick();
        }
    }

    private void nextCycle() {
        while (nextTickTime > 0L && (System.nanoTime() < nextTickTime)) {
            // Do nothing
        }
        nextTickTime += delayNanos;
    }
}
