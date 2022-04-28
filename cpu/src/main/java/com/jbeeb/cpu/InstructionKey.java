package com.jbeeb.cpu;

import java.util.Objects;

public final class InstructionKey {

    private final Instruction instruction;
    private final AddressMode addressMode;

    public InstructionKey(final Instruction instruction, final AddressMode addressMode) {
        this.instruction = Objects.requireNonNull(instruction);
        this.addressMode = Objects.requireNonNull(addressMode);
    }

    public Instruction getInstruction() {
        return instruction;
    }

    public AddressMode getAddressMode() {
        return addressMode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstructionKey that = (InstructionKey) o;
        return instruction == that.instruction && addressMode == that.addressMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(instruction, addressMode);
    }

    public String toString() {
        return instruction + "[" + addressMode + "]";
    }
}
