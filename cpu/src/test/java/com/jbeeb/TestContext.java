package com.jbeeb;

import com.jbeeb.assembler.Assembler;
import com.jbeeb.cpu.*;
import com.jbeeb.memory.Memory;
import com.jbeeb.util.DefaultScheduler;
import com.jbeeb.util.SystemStatus;
import com.jbeeb.util.Util;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public final class TestContext {

    private static final int CODE_START = 0x1000;

    private final Cpu cpu;
    private final Memory memory;
    private final Assembler assembler;

    public TestContext(final String... statements) {
        final InstructionSet instructionSet = new InstructionSet();
        this.memory = Memory.randomAccessMemory(0, 65536);
        this.memory.writeWord(Cpu.CODE_START_VECTOR, CODE_START);
        this.assembler = new Assembler(CODE_START, memory, instructionSet);
        this.assembler.assemble(statements);
        this.assembler.assemble("HLT");
        this.cpu = new Cpu(SystemStatus.NOP, new DefaultScheduler(), memory);
        this.cpu.setVerboseSupplier(() -> false);
    }

    public TestContext assemble(final String... statements) {
        assembler.assemble(statements);
        return this;
    }

    public TestContext assemble(final List<String> statements) {
        return assemble(statements.toArray(new String[]{}));
    }

    public TestContext resetAndAssemble(final String... statements) {
        resetCodePos();
        assemble(statements);
        assemble("HLT");
        cpu.reset();
        return this;
    }

    public void resetCodePos() {
        assembler.resetPos();
    }

    public Cpu getCpu() {
        return cpu;
    }

    public Memory getMemory() {
        return memory;
    }

    public void writeByte(final int address, final int value) {
        memory.writeByte(address, value);
    }

    public void writeWord(final int address, final int value) {
        memory.writeWord(address, value);
    }

    public int getA() {
        return cpu.getA();
    }

    public int getX() {
        return cpu.getX();
    }

    public int getY() {
        return cpu.getY();
    }

    public boolean isFlagSet(final Flag flag) {
        return cpu.isFlagSet(flag);
    }

    public boolean isFlagClear(final Flag flag) {
        return cpu.isFlagClear(flag);
    }

    public void assertNZCorrect(final int value) {
        assertThat(cpu.isFlagSet(Flag.NEGATIVE)).isEqualTo(Util.isNegative(value));
        assertThat(cpu.isFlagSet(Flag.ZERO)).isEqualTo(Util.isZero(value));
    }

    public static TestContext create(final String... statements) {
        return new TestContext(statements);
    }

    public static TestContext createAndRun(final String... statements) {
        final TestContext context = create(statements);
        context.run();
        return context;
    }

    public static TestContext createAbsolute(final Instruction instruction, final AddressMode addressMode, final int address, final int offset, final int value, final boolean carryIn) {
        Util.checkUnsignedWord(value);
        final TestContext context = create();
        context.resetCodePos();
        context.assemble(carryIn ? "SEC" : "CLC");
        final List<String> statements = new ArrayList<>();
        if (instruction.getType() == InstructionType.WRITE) {
            if (instruction != Instruction.STA) {
                throw new IllegalStateException();
            }
            statements.add("LDA #" + value);
        }
        switch (addressMode) {
            case ABSOLUTE:
                statements.add(instruction + " " + address);
                break;
            case ABSOLUTE_X:
                statements.add("LDX #" + offset);
                statements.add(instruction + " " + address + ",X");
                break;
            case ABSOLUTE_Y:
                statements.add("LDY #" + offset);
                statements.add(instruction + " " + address + ",Y");
                break;
            default:
                throw new IllegalStateException();
        }

        // Maybe write to memory
        switch (instruction.getType()) {
            case READ:
            case READ_MODIFY_WRITE:
                context.writeByte(address + offset, value);
                break;
            case WRITE:
                break;
            default:
                throw new IllegalStateException();
        }

        context.assemble(statements);
        context.assemble("HLT");
        context.run();
        return context;
    }

    public static TestContext createZeroPage(final String instruction, final AddressMode addressMode, final int address, final int offset, final int value, final String... statements) {
        Util.checkUnsignedByte(value);
        final TestContext context = create();

        switch (addressMode) {
            case ZPG:
                context.assemble(instruction + " " + address);
                break;
            case ZPG_X:
                context.assemble(instruction + " " + address + ",X");
                break;
            case ZPG_Y:
                context.assemble(instruction + " " + address + ",Y");
                break;
            default:
                throw new IllegalStateException();
        }
        context.writeByte((address + offset) & 0xFF, value);
        context.assemble(statements);
        context.run();
        return context;
    }

    public TestContext run() {
        cpu.run();
        return this;
    }
}
