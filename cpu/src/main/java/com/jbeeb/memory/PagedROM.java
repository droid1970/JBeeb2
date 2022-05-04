package com.jbeeb.memory;

import com.jbeeb.device.PagedRomSelect;

import java.util.Map;

public final class PagedROM implements Memory {

    private final PagedRomSelect selector;
    private final int startAddess;
    private final int size;
    private final ReadOnlyMemory[] roms;

    public PagedROM(
            int start,
            int size,
            PagedRomSelect selector,
            Map<Integer, ReadOnlyMemory> roms
    ) {
        this.startAddess = start;
        this.size = size;
        this.selector = selector;
        this.roms = new ReadOnlyMemory[16];
        roms.forEach((slot, rom) -> {
            this.roms[slot] = rom;
        });
    }

    @Override
    public boolean hasAddress(int address) {
        return (address >= startAddess && address < startAddess + size);
    }

    @Override
    public int readByte(int address) {
        final ReadOnlyMemory rom = roms[selector.getSelectedSlot()];
        return (rom == null) ?  0 : rom.readByte(address);
    }

    @Override
    public void writeByte(int address, int value) {
        // Do nothing
    }

    @Override
    public void installIntercept(int address, FetchIntercept intercept) {
        roms[selector.getSelectedSlot()].installIntercept(address, intercept);
    }

    @Override
    public void removeIntercept(int address) {
        roms[selector.getSelectedSlot()].removeIntercept(address);
    }

    @Override
    public boolean processIntercepts(int address) {
        return roms[selector.getSelectedSlot()].processIntercepts(address);
    }
}
