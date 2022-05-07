package com.jbeeb.clock;

public interface ClockListener {
    void tick(int clockRate);
    default void setPaused(final boolean paused) {
        // Do nothing by default
    }
}
