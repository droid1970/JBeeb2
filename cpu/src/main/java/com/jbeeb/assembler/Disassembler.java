package com.jbeeb.assembler;

import com.jbeeb.cpu.*;
import com.jbeeb.memory.Memory;

import java.util.Objects;

public final class Disassembler {

    private final InstructionSet instructionSet;
    private final Memory memory;

    public Disassembler(final InstructionSet instructionSet, final Memory memory) {
        this.instructionSet = Objects.requireNonNull(instructionSet);
        this.memory = Objects.requireNonNull(memory);
    }

    public String disassemble(int pc) {
        final StringBuilder s = new StringBuilder();
        final int opcode = memory.readByte(pc++);
        final InstructionKey key = instructionSet.decode(opcode);
        final Instruction instruction = key.getInstruction();
        final AddressMode addressMode = key.getAddressMode();
        int bytesToFollow = (instruction == Instruction.BRK) ? 1 : addressMode.getParameterByteCount();
        s.append(instruction);
        if (bytesToFollow == 1) {
            s.append(addressMode.formatOperand(pc, memory.readByte(pc)));
        } else if (bytesToFollow == 2) {
            s.append(addressMode.formatOperand(pc, memory.readWord(pc)));
        } else {
            s.append(addressMode.formatOperand(pc, 0));
        }
        return s.toString();
    }
}
