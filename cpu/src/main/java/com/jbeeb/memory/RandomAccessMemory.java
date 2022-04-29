package com.jbeeb.memory;

import com.jbeeb.util.StateKey;

@StateKey(key = "randomAccessMemory")
public final class RandomAccessMemory extends AbstractMemory {
    public RandomAccessMemory(int start, int size) {
        super(start, size, false);
    }
}
