package com.jbeeb.memory;

import com.jbeeb.cpu.InstructionSet;
import com.jbeeb.util.StateKey;
import com.jbeeb.util.Util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractMemory implements Memory {

    @StateKey(key = "start")
    private final int start;

    @StateKey(key = "memory")
    private final int[] memory;

    @StateKey(key = "readOnly")
    private final boolean readOnly;

    private Map<Integer, FetchIntercept> intercepts;

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
        final int ret = memory[computeIndex(address)];
        return ret;
    }

    @Override
    public void writeByte(int address, int value) {
        if (!readOnly) {
            checkWriteable();
            Util.checkUnsignedByte(value);
            memory[computeIndex(address)] = value;
        }
    }

    private void writeByteUnsafe(final int address, final int value) {
        memory[computeIndex(address)] = value;
    }

    @Override
    public void installIntercept(int address, FetchIntercept intercept) {
        if (intercepts == null) {
            intercepts = new HashMap<>();
        }
        intercepts.put(address, intercept);
        writeByteUnsafe(address, InstructionSet.RTS_OPCODE);
        Util.log("Intercept added at address " + Util.formatHexWord(address), 0);
    }

    @Override
    public void removeIntercept(int address) {
        if (intercepts != null) {
            intercepts.remove(address);
            if (intercepts.isEmpty()) {
                intercepts = null;
            }
        }
    }

    @Override
    public boolean processIntercepts(int address) {
        if (intercepts != null) {
            final FetchIntercept intercept = intercepts.get(address);
            if (intercept != null) {
                return intercept.run();
            }
        }
        return false;
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
