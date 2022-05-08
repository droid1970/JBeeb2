package com.jbeeb.assembler;

import com.jbeeb.cpu.*;
import com.jbeeb.memory.Memory;
import com.jbeeb.memory.MemoryWriter;
import com.jbeeb.util.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public final class Assembler {

    private final int codeStart;
    private final MemoryWriter memoryWriter;
    private final InstructionSet instructionSet;
    private final Map<String, Integer> variables = new HashMap<>();
    private final Map<String, List<String>> macros = new HashMap<>();

    public Assembler(final int codeStart, final Memory memory, final InstructionSet instructionSet) {
        this.codeStart = codeStart;
        this.memoryWriter = new MemoryWriter(memory);
        this.memoryWriter.setPos(codeStart);
        this.instructionSet = Objects.requireNonNull(instructionSet);
    }

    public void resetPos() {
        memoryWriter.setPos(codeStart);
    }

    public void assemble(final InputStream in) throws IOException {
        final List<String> statements = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(in))) {
            String line;
            while ((line = r.readLine()) != null) {
                statements.add(line);
            }
        }
        assemble(statements);
    }

    public void assemble(final List<String> statements) {
        final int pos = memoryWriter.getPos();
        assemble(statements, true);
        macros.clear();
        memoryWriter.setPos(pos);
        assemble(statements, false);
    }

    public void assemble(final String... statements) {
        assemble(Arrays.asList(statements));
    }

    private void assemble(final List<String> statements, boolean firstPass) {
        for (String s : statements) {
            assembleStatement(s, firstPass);
        }
    }

    private void assembleStatement(final String statement, final boolean firstPass) {
        final OpAddrMode result = inferInstructionAndAddressMode(statement, firstPass);
        if (result != null) {
            result.validate(instructionSet);
            final List<Integer> bytes = instructionSet.encode(result.instruction, result.addressMode, result.operand);
            memoryWriter.writeBytesToPos(bytes);
        }
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

    private String definingMacroName;
    private boolean definingMacro;

    @SuppressWarnings("squid:S3776")
    private OpAddrMode inferInstructionAndAddressMode(String statement, final boolean firstPass) {
        statement = statement.strip();

        if ("endm".equals(statement)) {
            definingMacro = false;
            definingMacroName = null;
            return null;
        }

        if (definingMacro) {
            macros.computeIfAbsent(definingMacroName, k -> new ArrayList<>()).add(statement);
            return null;
        }

        //
        // Remove comments
        //
        if (statement.contains(";")) {
            statement = statement.substring(0, statement.indexOf(";")).strip();
        }

        if (statement.isEmpty()) {
            return null;
        }

        // Does statement start with an unrecognised variable or instruction?

        String[] toks = statement.split(" +");
        final String firstToken = toks[0];
        if (!isInstruction(firstToken)) {
            if (isMacro(firstToken)) {
                final List<String> macroDefinition = macros.get(firstToken);
                for (String s : macroDefinition) {
                    assembleStatement(s, firstPass);
                }
                return null;
            } else if (toks.length == 1 || isInstruction(toks[1])) {
                // Code position variable
                if (firstPass) {
                    final String variableName = toks[0].replace(".", "");
                    variables.put(variableName, memoryWriter.getPos());
                }
                if (toks.length > 1) {
                    toks = Arrays.copyOfRange(toks, 1, toks.length);
                } else {
                    return null;
                }
            } else if (statement.contains("=")) {
                // Variable declaration
                final int equalsIndex = statement.indexOf("=");
                final String variableName = statement.substring(0, equalsIndex).strip();
                final String value = statement.substring(equalsIndex + 1).strip();
                variables.put(variableName, parseOperand(value, firstPass));
                return null;
            } else if ("macro".equals(toks[1])) {
                definingMacro = true;
                definingMacroName = firstToken;
                return null;
            }
        }

        final Instruction instruction = Instruction.valueOf(toks[0].toUpperCase());

        final Set<AddressMode> supportedAddressModes = instructionSet.getSupportedAddressModes(instruction);

        if (toks.length == 1) {
            return new OpAddrMode(instruction, 0, AddressMode.IMPLIED);
        }

        String operand = toks[1];
        if ("A".equals(operand)) {
            return new OpAddrMode(instruction, 0, AddressMode.ACCUMULATOR);
        }

        final Optional<TestCode> optTestCode = TestCode.of(operand);
        if (optTestCode.isPresent()) {
            return new OpAddrMode(instruction, optTestCode.get().ordinal(), AddressMode.IMMEDIATE);
        }

        if (operand.startsWith("#")) {
            return new OpAddrMode(instruction, parseOperand(operand.substring(1), firstPass), AddressMode.IMMEDIATE);
        }

        if (operand.startsWith("(") && operand.endsWith(")")) {
            operand = operand.substring(1, operand.length() - 1);
            if (operand.endsWith(",X")) {
                final int operandValue = parseOperand(operand.substring(0, operand.length() - 2), firstPass);
                checkIsUnsignedByte(operandValue);
                return new OpAddrMode(instruction, operandValue, AddressMode.X_INDIRECT);
            } else {
                return new OpAddrMode(instruction, parseOperand(operand, firstPass), AddressMode.INDIRECT);
            }
        }

        if (operand.startsWith("(")) { // Doesn't end with )
            operand = operand.substring(1);
            if (operand.endsWith("),Y")) {
                final int operandValue = parseOperand(operand.substring(0, operand.length() - 3), firstPass);
                checkIsUnsignedByte(operandValue);
                return new OpAddrMode(instruction, operandValue, AddressMode.INDIRECT_Y);
            }
        }

        // Doesn't start with (
        if (operand.endsWith(",X")) {
            int operandValue = parseOperand(operand.substring(0, operand.length() - 2), firstPass);
            final AddressMode am;
            if (operandValue >= 0 && operandValue <= 255 && supportedAddressModes.contains(AddressMode.ZPG_X)) {
                am = AddressMode.ZPG_X;
            } else {
                am = AddressMode.ABSOLUTE_X;
            }
            return new OpAddrMode(instruction, operandValue, am);
        }

        if (operand.endsWith(",Y")) {
            int operandValue = parseOperand(operand.substring(0, operand.length() - 2), firstPass);
            final AddressMode am;
            if (operandValue >= 0 && operandValue <= 255 && supportedAddressModes.contains(AddressMode.ZPG_Y)) {
                am = AddressMode.ZPG_Y;
            } else {
                am = AddressMode.ABSOLUTE_Y;
            }
            return new OpAddrMode(instruction, operandValue, am);
        }

        if (instruction.getType() == InstructionType.BRANCH) {
            final int operandValue = parseOperand(operand, firstPass);
            final int diff = operandValue - (memoryWriter.getPos() + 2);
            if (!firstPass) {
                checkIsSignedByte(diff);
            }
            return new OpAddrMode(instruction, diff & 0xFF, AddressMode.RELATIVE);
        }

        final int operandValue = parseOperand(operand, firstPass);
        final AddressMode am;
        if (operandValue >= 0 && operandValue <= 255 && supportedAddressModes.contains(AddressMode.ZPG)) {
            am = AddressMode.ZPG;
        } else {
            am = AddressMode.ABSOLUTE;
        }
        return new OpAddrMode(instruction, operandValue, am);
    }

    private boolean isInstruction(final String s) {
        try {
            Instruction.valueOf(s.toUpperCase());
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean isMacro(final String s) {
        return macros.containsKey(s);
    }

    private boolean isVariable(final String s) {
        return variables.containsKey(s);
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

    private int parseOperand(final String operand, final boolean ignoreUnknownVariables) {
        try {
            if (operand.startsWith("$")) {
                return Integer.parseInt(operand.substring(1), 16);
            } else {
                return Integer.parseInt(operand);
            }
        } catch (Exception ex) {
            // Failed to parse operand. Try variables
            if (variables.containsKey(operand)) {
                return variables.get(operand);
            } else {
                if (ignoreUnknownVariables) {
                    return 0;
                } else {
                    throw new IllegalStateException("Could not parse operand - " + operand);
                }
            }
        }
    }
}
