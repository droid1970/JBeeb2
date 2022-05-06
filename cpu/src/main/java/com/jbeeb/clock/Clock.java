package com.jbeeb.clock;

import com.jbeeb.util.SystemStatus;

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

    private ClockSpeed clockSpeed = ClockSpeed.CR200;
    private long initialDelayNanos;
    private long delayNanos;

    private long nextTickTime;

    public Clock(
            final SystemStatus systemStatus,
            final ClockSpeed clockSpeed,
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

    public ClockSpeed getClockSpeed() {
        return clockSpeed;
    }

    public void setClockSpeed(final ClockSpeed clockSpeed) {
        this.clockSpeed = Objects.requireNonNull(clockSpeed);
        this.delayNanos = 1_000_000_000L / clockSpeed.getClockRate();
        this.initialDelayNanos = this.delayNanos;
    }

    public long getCycleCount() {
        return cycleCount;
    }

    public void run(final BooleanSupplier stopCondition) {
        long startTime = System.nanoTime();
        this.nextTickTime = startTime + delayNanos;
        while (!stopCondition.getAsBoolean()) {
            awaitNextCycle();
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
        final double delta = cps / clockSpeed.getClockRate();
        delayNanos = Math.min(initialDelayNanos, Math.max(10L, (long) (delayNanos * delta)));
    }

    private void tick() {
        for (ClockListener l : listeners) {
            l.tick();
        }
    }

    private void awaitNextCycle() {
        while (clockSpeed.isThrottled() && nextTickTime > 0L && (System.nanoTime() < nextTickTime)) {
            // Do nothing
        }
        nextTickTime += delayNanos;
    }
}
