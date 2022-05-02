package com.jbeeb.memory;

import com.jbeeb.device.MemoryMappedDevice;
import com.jbeeb.util.Util;

import java.util.ArrayList;
import java.util.List;

public interface Memory {

    int KB = 1024;

    boolean hasAddress(int address);
    int readByte(int address);
    void writeByte(int address, int value);

    default int readWord(int address) {
        Util.checkUnsignedWord(address);
        final int lo = readByte(address);
        final int hi = readByte(address + 1);
        return (lo & 0xFF) | ((hi & 0xFF) << 8);
    }

    default void writeWord(int address, int word) {
        Util.checkUnsignedWord(word);
        writeByte(address, word & 0xff);
        writeByte(address + 1, (word >>> 8) & 0xFF);
    }

    static Memory readOnlyKb(final int start, final int[] data) {
        return new ReadOnlyMemory(start, data);
    }
    static Memory randomAccessMemory(final int start, final int size) {
        return new RandomAccessMemory(start, size);
    }

    static Memory bbcMicroB(final List<MemoryMappedDevice> devices, final Memory ram, final Memory pagedRom, final Memory osRom) {
        final List<Memory> regions = new ArrayList<>();
        regions.addAll(devices);
        regions.add(ram);
        regions.add(pagedRom);
        regions.add(osRom);
        return new CompoundMemory(regions);
    }
}
