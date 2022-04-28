package com.jbeeb.memory;

import java.util.ArrayList;
import java.util.List;

public final class CompoundMemory implements Memory {

    private final List<Memory> regions = new ArrayList<>();

    public CompoundMemory(final List<Memory> regions) {
        this.regions.addAll(regions);
    }

    private Memory getRegion(final int address) {
        if (address == 14000) {
            int x = 1;
        }
        for (Memory m : regions) {
            if (m.hasAddress(address)) {
                return m;
            }
        }
        throw cannotAccessException(address);
        //return regions.stream().filter(r -> r.hasAddress(address)).findFirst().orElseThrow(() -> cannotAccessException(address));
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
}
