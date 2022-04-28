package com.jbeeb;

import com.jbeeb.device.AbstractMemoryMappedDevice;

public final class SheilaMemoryMappedDevice extends AbstractMemoryMappedDevice {

    public SheilaMemoryMappedDevice() {
        super("sheila", 0xFE00, 0x100);
    }

    @Override
    public int readRegister(int index) {
        return 0;
    }

    @Override
    public void writeRegister(int index, int value) {

    }
}
