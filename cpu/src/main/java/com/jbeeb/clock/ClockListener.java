package com.jbeeb.clock;

public interface ClockListener {
    void tick();

    default boolean canSaveState() {
        return true;
    }
}
