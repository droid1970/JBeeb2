package com.jbeeb.clock;

public interface ClockListener {
    void tick(ClockSpeed clockSpeed, long elapsedNanos);

    default void setPaused(final boolean paused) {
        // Do nothing by default
    }
}
