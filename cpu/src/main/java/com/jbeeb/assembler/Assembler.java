package com.jbeeb.assembler;

import com.jbeeb.cpu.*;
import com.jbeeb.memory.Memory;
import com.jbeeb.memory.MemoryWriter;
import com.jbeeb.util.Util;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class Assembler {

    private final int codeStart;
    private final MemoryWriter memoryWriter;
    private final InstructionSet instructionSet;

    public Assembler(final int codeStart, final Memory memory, final InstructionSet instructionSet) {
        this.codeStart = codeStart;
        this.memoryWriter = new MemoryWriter(memory);
        this.memoryWriter.setPos(codeStart);
        this.instructionSet = Objects.requireNonNull(instructionSet);
    }

    public void resetPos() {
        memoryWriter.setPos(codeStart);
    }

    public void assemble(final List<String> statements) {
        for (String s : statements) {
            final OpAddrMode result = inferInstructionAndAddressMode(s);
            result.validate(instructionSet);
            memoryWriter.writeBytesToPos(instructionSet.encode(result.instruction, result.addressMode, result.operand));
        }
    }

    public void assemble(final String... statements) {
        assemble(Arrays.asList(statements));
    }

    private static final class OpAddrMode {
        private final Instruction instruction;
        private final int operand;
        private final AddressMode addressMode;

        public OpAddrMode(Instruction instruction, int operand, AddressMode addressMode) {
            this.instruction = Objects.requireNonNull(instruction);
            this.operand = Objects.requireNonNull(operand);
            this.addressMode = Objects.requireNonNull(addressMode);
        }

        void validate(final InstructionSet instructionSet) {
            if (!instructionSet.isAddressModeSupported(instruction, addressMode)) {
                throw new IllegalStateException(instruction + " does not support address mode " + addressMode);
            }
        }

        @Override
        public String toString() {
            final int effectiveOperand = (addressMode == AddressMode.RELATIVE) ? Util.signed(operand) : operand;
            return instruction + " (" + addressMode + ") operand = " + effectiveOperand;
        }
    }

    @SuppressWarnings("squid:S3776")
    private OpAddrMode inferInstructionAndAddressMode(String statement) {
        statement = statement.strip();

        final String[] toks = statement.split(" +");
        final Instruction instruction = Instruction.valueOf(toks[0]);

        final Set<AddressMode> supportedAddressModes = instructionSet.getSupportedAddressModes(instruction);

        if (toks.length == 1) {
            return new OpAddrMode(instruction, 0, AddressMode.IMPLIED);
        }

        String operand = toks[1];
        if ("A".equals(operand)) {
            return new OpAddrMode(instruction, 0, AddressMode.ACCUMULATOR);
        }

        if (operand.startsWith("#")) {
            return new OpAddrMode(instruction, parseOperand(operand.substring(1)), AddressMode.IMMEDIATE);
        }

        if (operand.startsWith("(") && operand.endsWith(")")) {
            operand = operand.substring(1, operand.length() - 1);
            if (operand.endsWith(",X")) {
                final int operandValue = parseOperand(operand.substring(0, operand.length() - 2));
                checkIsUnsignedByte(operandValue);
                return new OpAddrMode(instruction, operandValue, AddressMode.X_INDIRECT);
            } else {
                return new OpAddrMode(instruction, parseOperand(operand), AddressMode.INDIRECT);
            }
        }

        if (operand.startsWith("(")) { // Doesn't end with )
            operand = operand.substring(1);
            if (operand.endsWith("),Y")) {
                final int operandValue = parseOperand(operand.substring(0, operand.length() - 3));
                checkIsUnsignedByte(operandValue);
                return new OpAddrMode(instruction, operandValue, AddressMode.INDIRECT_Y);
            }
        }

        // Doesn't start with (
        if (operand.endsWith(",X")) {
            int operandValue = parseOperand(operand.substring(0, operand.length() - 2));
            final AddressMode am;
            if (operandValue >= 0 && operandValue <= 255 && supportedAddressModes.contains(AddressMode.ZPG_X)) {
                am = AddressMode.ZPG_X;
            } else {
                am = AddressMode.ABSOLUTE_X;
            }
            return new OpAddrMode(instruction, operandValue, am);
        }

        if (operand.endsWith(",Y")) {
            int operandValue = parseOperand(operand.substring(0, operand.length() - 2));
            final AddressMode am;
            if (operandValue >= 0 && operandValue <= 255 && supportedAddressModes.contains(AddressMode.ZPG_Y)) {
                am = AddressMode.ZPG_Y;
            } else {
                am = AddressMode.ABSOLUTE_Y;
            }
            return new OpAddrMode(instruction, operandValue, am);
        }

        if (instruction.getType() == InstructionType.BRANCH) {
            final int operandValue = parseOperand(operand);
            checkIsSignedByte(operandValue);
            return new OpAddrMode(instruction, operandValue & 0xFF, AddressMode.RELATIVE);
        }

        final int operandValue = parseOperand(operand);
        final AddressMode am;
        if (operandValue >= 0 && operandValue <= 255 && supportedAddressModes.contains(AddressMode.ZPG)) {
            am = AddressMode.ZPG;
        } else {
            am = AddressMode.ABSOLUTE;
        }
        return new OpAddrMode(instruction, operandValue, am);
    }

    private static int checkIsUnsignedByte(final int operandValue) {
        if (operandValue < 0 || operandValue > 255) {
            throw new IllegalStateException(operandValue + ": operand out of byte rangd");
        }
        return operandValue;
    }

    private static int checkIsSignedByte(final int operandValue) {
        if (operandValue < -128 || operandValue > 127) {
            throw new IllegalStateException(operandValue + ": operand out of signed byte range");
        }
        return operandValue;
    }

    private static int parseOperand(final String operand) {
        if (operand.startsWith("$")) {
            return Integer.parseInt(operand.substring(1), 16);
        } else {
            return Integer.parseInt(operand);
        }
    }
}
