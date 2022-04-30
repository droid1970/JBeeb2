package com.jbeeb.memory;

import com.jbeeb.util.StateKey;
import com.jbeeb.util.Util;

import java.util.Arrays;

public abstract class AbstractMemory implements Memory {

    @StateKey(key = "start")
    private final int start;

    @StateKey(key = "memory")
    private final int[] memory;

    @StateKey(key = "readOnly")
    private final boolean readOnly;

    public AbstractMemory(final int start, final int size, final boolean readOnly) {
        this(start, new int[size], readOnly);
    }

    public AbstractMemory(final int start, final int[] memory, final boolean readOnly) {
        this.start = start;
        this.memory = Arrays.copyOf(memory, memory.length);
        this.readOnly = readOnly;
    }

    @Override
    public boolean hasAddress(int address) {
        return address >= start && address < start + memory.length;
    }

    @Override
    public int readByte(int address) {
        return memory[computeIndex(address)];
    }

    @Override
    public void writeByte(int address, int value) {
        checkWriteable();
        Util.checkUnsignedByte(value);
        memory[computeIndex(address)] = value;
    }

    private int computeIndex(final int address) {
        if (!hasAddress(address)) {
            throw new IllegalStateException(address + ": address out of range");
        }
        return address - start;
    }

    private void checkWriteable() {
        if (true) return;
        if (readOnly) {
            throw new IllegalStateException("memory is read-only");
        }
    }
}
