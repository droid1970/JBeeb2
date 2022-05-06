package com.jbeeb.memory;

import java.util.ArrayList;
import java.util.List;

public final class CompoundMemory implements Memory {

    private final int minAddress;
    private final int maxAddress;

    private final List<Memory> regions = new ArrayList<>();
    private final Memory[] map;

    public CompoundMemory(final List<Memory> regions) {
        this.regions.addAll(regions);
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (Memory m : regions) {
            final int rmin = m.getMinAddress();
            final int rmax = m.getMaxAddress();
            if (rmin < min) {
                min = rmin;
            }
            if (rmax > max) {
                max = rmax;
            }
        }
        this.minAddress = min;
        this.maxAddress = max;
        this.map = new Memory[maxAddress + 1];
        for (int address = min; address <= max; address++) {
            this.map[address] = computeRegion(address);
        }
    }

    private Memory computeRegion(final int address) {
        for (Memory m : regions) {
            if (m.hasAddress(address)) {
                return m;
            }
        }
        return null;
    }

    private Memory getRegion(final int address) {
        final Memory m = map[address];
        if (m == null) {
            throw cannotAccessException(address);
        }
        return m;
    }

    private IllegalStateException cannotAccessException(final int address) {
        return new IllegalStateException("cannot access address " + Integer.toHexString(address));
    }

    @Override
    public boolean hasAddress(int address) {
        return regions.stream().anyMatch(r -> r.hasAddress(address));
    }

    @Override
    public int getMinAddress() {
        return minAddress;
    }

    @Override
    public int getMaxAddress() {
        return maxAddress;
    }

    @Override
    public int readByte(int address) {
        final Memory m = map[address];
        if (m != null) {
            return m.readByte(address);
        }
        throw cannotAccessException(address);
    }

    @Override
    public void writeByte(int address, int value) {
        final Memory m = map[address];
        if (m != null) {
            m.writeByte(address, value);
        } else {
            throw cannotAccessException(address);
        }
    }

    @Override
    public int readWord(int address) {
        return getRegion(address).readWord(address);
    }

    @Override
    public void writeWord(int address, int value) {
        getRegion(address).writeWord(address, value);
    }

    @Override
    public void installIntercept(int address, FetchIntercept intercept, boolean addRTS) {
        getRegion(address).installIntercept(address, intercept, addRTS);
    }

    @Override
    public void removeIntercept(int address) {
        getRegion(address).removeIntercept(address);
    }

    @Override
    public boolean processIntercepts(int address) {
        return getRegion(address).processIntercepts(address);
    }
}
