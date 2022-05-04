package com.jbeeb.device;

import com.jbeeb.util.SystemStatus;

public final class PagedRomSelect extends AbstractMemoryMappedDevice {

    private int slot = 15;

    public PagedRomSelect(SystemStatus systemStatus, String name, int startAddress, int size) {
        super(systemStatus, name, startAddress, size);
    }

    @Override
    public int readRegister(int index) {
        // Read-only
        return 0;
    }

    @Override
    public void writeRegister(int index, int value) {
        this.slot = (value & 0xf);
    }

    public int getSelectedSlot() {
        return slot;
    }

    public void setSelectedSlot(int slot) {
        this.slot = slot;
    }
}
