package com.jbeeb.util;

public interface StatusProducer {
    default SystemStatus getSystemStatus() {
        return SystemStatus.NOP;
    }
}
