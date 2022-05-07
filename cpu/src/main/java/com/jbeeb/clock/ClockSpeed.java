package com.jbeeb.clock;

import java.util.Arrays;
import java.util.Objects;

public final class ClockSpeed {

    public static final int MHZ = 1_000_000;    // Don't change this
    public static final int TWO_MHZ = 2 * MHZ;  // Don't change this
    public static final int FIFTY_HZ = 50;      // Don't change this

    public static final ClockSpeed CR200 = new ClockSpeed("2.0 Mhz", 2 * MHZ, true);
    public static final ClockSpeed CR400 = new ClockSpeed("4.0 Mhz", 4 * MHZ, true);
    public static final ClockSpeed CR600 = new ClockSpeed("6.0 Mhz", 6 * MHZ, true);
    public static final ClockSpeed CR800 = new ClockSpeed("8.0 Mhz", 8 * MHZ, true);

    public static final ClockSpeed MAX = new ClockSpeed("Maximum", 397 * MHZ, false);

    private final String displayName;
    private final int clockRate;
    private final boolean throttled;
    private final boolean fitsExactly;

    private static final ClockSpeed[] STANDARD_VALUES = {
            CR200,
            CR400,
            CR600,
            CR800,
            MAX
    };

    public ClockSpeed(final String displayName, final int clockRate, final boolean throttled) {
        this.displayName = Objects.requireNonNull(displayName);
        this.clockRate = clockRate;
        this.throttled = throttled;
        this.fitsExactly = fitsExactly(clockRate);
    }

    private static boolean fitsExactly(final int clockRate) {
        if (clockRate == TWO_MHZ) {
            return true;
        } else if (clockRate > TWO_MHZ) {
            return ((clockRate / TWO_MHZ) * TWO_MHZ) == clockRate;
        } else {
            return ((TWO_MHZ / clockRate) * clockRate) == TWO_MHZ;
        }
    }

    public int computeElapsedCycles(final int CLOCK_RATE, final long inputCycleCount, final long myCycleCount, final long elapsedNanos) {
        int cycles = 0;
        if (fitsTwoMhz()) {
            if (this.clockRate > CLOCK_RATE) {
                // Maybe skip this
                final int stretch = this.clockRate / CLOCK_RATE;
                if ((inputCycleCount % stretch) != 0) {
                    return 0;
                } else {
                    return 1;
                }
            } else {
                return CLOCK_RATE / clockRate;
            }
        } else {
            long cyclesSince = ClockSpeed.computeElapsedCycles(CLOCK_RATE, elapsedNanos);
            return Math.max(0, (int) (cyclesSince - myCycleCount));
        }
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getClockRate() {
        return clockRate;
    }

    public boolean isThrottled() {
        return throttled;
    }

    public boolean fitsTwoMhz() {
        return fitsExactly;
    }

    public static long computeElapsedCycles(final int targetRate, final long elapsedNanos) {
        final long nanosPerCycle = 1_000_000_000L / targetRate;
        return elapsedNanos / nanosPerCycle;
    }

    public static ClockSpeed[] getStandardValues() {
        return Arrays.copyOf(STANDARD_VALUES, STANDARD_VALUES.length);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClockSpeed that = (ClockSpeed) o;
        return clockRate == that.clockRate && throttled == that.throttled;
    }

    @Override
    public int hashCode() {
        return Objects.hash(clockRate, throttled);
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
