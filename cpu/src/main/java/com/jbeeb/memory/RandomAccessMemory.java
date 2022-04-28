package com.jbeeb.memory;

public final class RandomAccessMemory extends AbstractMemory {
    public RandomAccessMemory(int start, int size) {
        super(start, size, false);
    }
}
