package com.jbeeb.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;

public final class Clock {

    private static final long RESET_CYCLES = 2_000_000L;
    private static final long ADJUST_MASK = 0xFF;

    private final SystemStatus systemStatus;
    private final ClockListener[] listeners;
    private final long maxCycleCount;
    private long cycleCount;
    private long cycleCountSinceReset;

    private long clockSpeed = 2_000_000;
    private long initialDelayNanos;
    private long delayNanos;

    private long nextTickTime;

    private volatile boolean throttled = true;

    public Clock(
            final SystemStatus systemStatus,
            final int clockSpeed,
            final long maxCycleCount,
            final List<ClockListener> listeners
    ) {
        this.systemStatus = Objects.requireNonNull(systemStatus);
        setClockSpeed(clockSpeed);
        this.maxCycleCount = maxCycleCount;
        this.listeners = new ClockListener[listeners.size()];
        for (int i = 0; i < listeners.size(); i++) {
            this.listeners[i] = listeners.get(i);
        }
    }

    public synchronized void setClockSpeed(final int clockSpeed) {
        this.clockSpeed = clockSpeed;
        this.delayNanos = 1_000_000_000L / clockSpeed;
        this.initialDelayNanos = delayNanos;
    }

    public boolean isFullSpeed() {
        return !throttled;
    }

    public void setFullSpeed(final boolean fullSpeed) {
        this.throttled = !fullSpeed;
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
        final double delta = cps / clockSpeed;
        delayNanos = Math.min(initialDelayNanos, Math.max(10L, (long) (delayNanos * delta)));
    }

    private Runnable saveStateCallback;

    private void tick() {
        for (ClockListener l : listeners) {
            l.tick();
        }
    }

    private void nextCycle() {
        while (throttled && nextTickTime > 0L && (System.nanoTime() < nextTickTime)) {
            // Do nothing
        }
        nextTickTime += delayNanos;
    }
}
