package com.jbeeb.device;

import com.jbeeb.util.StateKey;
import com.jbeeb.util.StatusProducer;
import com.jbeeb.util.SystemStatus;

import java.util.Objects;
import java.util.UUID;

public abstract class AbstractMemoryMappedDevice implements MemoryMappedDevice, StatusProducer {

    private final SystemStatus systemStatus;

    private final String id;
    private final String name;

    private final int startAddress;
    private final int endAddress;

    public AbstractMemoryMappedDevice(final SystemStatus systemStatus, final String name, final int startAddress, final int size) {
        this.systemStatus = Objects.requireNonNull(systemStatus);
        this.id = UUID.randomUUID().toString();
        this.name = Objects.requireNonNull(name);
        this.startAddress = startAddress;
        this.endAddress = startAddress + size - 1;
    }

    @Override
    public SystemStatus getSystemStatus() {
        return systemStatus;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public final boolean hasAddress(int address) {
        return (address >= startAddress && address <= endAddress);
    }

    @Override
    public final int readByte(int address) {
        return readRegister(address - startAddress) & 0xFF;
    }

    @Override
    public final void writeByte(int address, int value) {
        writeRegister(address - startAddress, value & 0xFF);
    }

    public abstract int readRegister(int index);
    public abstract void writeRegister(int index, int value);
}
