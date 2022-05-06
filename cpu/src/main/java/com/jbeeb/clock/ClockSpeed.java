package com.jbeeb.clock;

import java.util.Arrays;
import java.util.Objects;

public final class ClockSpeed {

    public static final ClockSpeed CR025 = new ClockSpeed("0.25 Mhz", 250_000, true);
    public static final ClockSpeed CR050 = new ClockSpeed("0.50 Mhz", 500_000, true);
    public static final ClockSpeed CR100 = new ClockSpeed("1.00 Mhz", 1_000_000, true);
    public static final ClockSpeed CR200 = new ClockSpeed("2.00 Mhz", 2_000_000, true);
    public static final ClockSpeed CR400 = new ClockSpeed("4.00 Mhz", 4_000_000, true);
    public static final ClockSpeed MAX = new ClockSpeed("Maximum", 1_000_000_000, true);

    private final String displayName;
    private final int clockRate;
    private final boolean throttled;

    private static final ClockSpeed[] STANDARD_VALUES = {
            CR025,
            CR050,
            CR100,
            CR200,
            CR400,
            MAX
    };

    public ClockSpeed(final String displayName, final int clockRate, final boolean throttled) {
        this.displayName = Objects.requireNonNull(displayName);
        this.clockRate = clockRate;
        this.throttled = throttled;
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
