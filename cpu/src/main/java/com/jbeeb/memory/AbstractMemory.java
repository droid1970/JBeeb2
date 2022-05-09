package com.jbeeb.memory;

import com.jbeeb.cpu.InstructionSet;
import com.jbeeb.util.StateKey;
import com.jbeeb.util.Util;

import java.util.*;
import java.util.function.IntConsumer;

public abstract class AbstractMemory implements Memory {

    @StateKey(key = "start")
    private final int start;

    @StateKey(key = "memory")
    private final int[] memory;

    @StateKey(key = "readOnly")
    private final boolean readOnly;

    private Map<Integer, FetchIntercept> intercepts;
    private Map<Integer, IntConsumer> modifyWatches = null;

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
    public int getMinAddress() {
        return start;
    }

    @Override
    public int getMaxAddress() {
        return start + memory.length - 1;
    }

    @Override
    public int readByte(int address) {
        final int ret = memory[computeIndex(address)];
        return ret;
    }

    @Override
    public void writeByte(int address, int value) {
        if (!readOnly) {
            if (modifyWatches != null && modifyWatches.containsKey(address)) {
                final int oldValue = readByte(address);
                if (oldValue != value) {
                    System.out.println(Util.formatHexWord(address) + ": " + oldValue + " -> " + value);
                    modifyWatches.get(address).accept(value);
                }
            }
            checkWriteable();
            Util.checkUnsignedByte(value);
            memory[computeIndex(address)] = value;
        }
    }

    private void writeByteUnsafe(final int address, final int value) {
        memory[computeIndex(address)] = value;
    }

    @Override
    public void installIntercept(int address, FetchIntercept intercept, boolean addRTS) {
        if (intercepts == null) {
            intercepts = new HashMap<>();
        }
        intercepts.put(address, intercept);
        if (addRTS) {
            writeByteUnsafe(address, InstructionSet.RTS_OPCODE);
        }
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

    public void addModifyWatch(final int address, final IntConsumer valueConsumer) {
        if (modifyWatches == null) {
            modifyWatches = new HashMap<>();
        }
        modifyWatches.put(address, valueConsumer);
    }
}
