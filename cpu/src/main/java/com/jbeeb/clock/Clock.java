package com.jbeeb.clock;

import com.jbeeb.util.SystemStatus;
import com.jbeeb.util.Util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;

public final class Clock {

    private static final int MAX_RESET_CYCLES = 8_000_000;
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

    private long pausedTime = 0L;

    private volatile boolean paused;

    public void setPaused(final boolean paused) {
        for (ClockListener l : listeners) {
            l.setPaused(paused);
        }

        if (paused) {
            this.paused = true;
        } else {
            this.paused = false;
        }
    }

    public void run(final BooleanSupplier stopCondition) {
        long firstStartTime = System.nanoTime();
        long resetTime = firstStartTime;
        this.nextTickTime = resetTime + delayNanos;
        while (!stopCondition.getAsBoolean()) {
            if (paused) {
                while (paused) {
                    final long t = System.nanoTime();
                    Util.sleep(100);
                    firstStartTime += System.nanoTime() - t;
                }
                resetTime = resetDelay(resetTime, false);
            }

            final long nanoTime = awaitNextCycle();

            // Send tick to all the listeners
            for (ClockListener l : listeners) {
                l.tick(clockSpeed, nanoTime - firstStartTime);
            }

            cycleCount++;
            cycleCountSinceReset++;
            if (cycleCount >= maxCycleCount) {
                return;
            }
            if ((cycleCountSinceReset & ADJUST_MASK) == 0) {
                adjustDelay(System.nanoTime() - resetTime);
            }

            // Reset every second or so
            if (cycleCountSinceReset >= Math.min(clockSpeed.getClockRate(), MAX_RESET_CYCLES)) {
                resetTime = resetDelay(resetTime, true);
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

    private long resetDelay(final long resetTime, final boolean updateStatus) {
        delayNanos = initialDelayNanos;
        final long now = System.nanoTime();
        if (updateStatus) {
            updateSystemStatus(now - resetTime);
        }
        cycleCountSinceReset = 0L;
        nextTickTime = now + delayNanos;
        return now;
    }

    private long awaitNextCycle() {
        long nanoTime;
        do {
            nanoTime = System.nanoTime();
        } while (clockSpeed.isThrottled() && nextTickTime > 0L && ((nextTickTime - nanoTime) > 0));
        nextTickTime += delayNanos;
        return nanoTime;
    }
}
