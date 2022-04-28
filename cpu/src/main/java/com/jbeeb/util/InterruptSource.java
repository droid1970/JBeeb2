package com.jbeeb.util;

public interface InterruptSource {
    String getName();
    boolean isIRQ();
    boolean isNMI();
}
