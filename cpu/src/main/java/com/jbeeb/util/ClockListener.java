package com.jbeeb.util;

public interface ClockListener {
    void tick();

    default boolean canSaveState() {
        return true;
    }
}
