package com.jbeeb.util;

public interface Interruptible {
    default void setNMI(final boolean nmi) {
        // Do nothing
    }
    void setIRQ(boolean irq);
}
