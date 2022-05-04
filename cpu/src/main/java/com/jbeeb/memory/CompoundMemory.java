package com.jbeeb.memory;

import java.util.ArrayList;
import java.util.List;

public final class CompoundMemory implements Memory {

    private final List<Memory> regions = new ArrayList<>();

    public CompoundMemory(final List<Memory> regions) {
        this.regions.addAll(regions);
    }

    private Memory getRegion(final int address) {
        for (Memory m : regions) {
            if (m.hasAddress(address)) {
                return m;
            }
        }
        throw cannotAccessException(address);
    }

    private IllegalStateException cannotAccessException(final int address) {
        return new IllegalStateException("cannot access address " + Integer.toHexString(address));
    }

    @Override
    public boolean hasAddress(int address) {
        return regions.stream().anyMatch(r -> r.hasAddress(address));
    }

    @Override
    public int readByte(int address) {
        return getRegion(address).readByte(address);
    }

    @Override
    public void writeByte(int address, int value) {
        getRegion(address).writeByte(address, value);
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
    public void installIntercept(int address, FetchIntercept intercept) {
        getRegion(address).installIntercept(address, intercept);
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
