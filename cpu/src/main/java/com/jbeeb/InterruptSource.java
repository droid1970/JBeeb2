package com.jbeeb;

public interface InterruptSource {
    String getName();
    boolean isIRQ();
    boolean isNMI();
}
