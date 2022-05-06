package com.jbeeb.cpu;

import com.jbeeb.util.Util;

import java.util.*;

import static com.jbeeb.cpu.Instruction.*;

public final class InstructionSet {

    public static final int RTS_OPCODE = 0x60;

    private final Map<InstructionKey, Integer> instructionToCode = new HashMap<>();
    private final InstructionKey[] codeToInstruction = new InstructionKey[256];
    private final Map<Instruction, Set<AddressMode>> instructionSupportedAddressModes = new EnumMap<>(Instruction.class);
    private final Map<AddressMode, Set<Instruction>> addressModeToInstructions = new EnumMap<>(AddressMode.class);

    public InstructionSet() {

        // Special HALT instruction (for debugging and testing)
        register(HLT, AddressMode.IMPLIED, 0x02);

        register(ADC, AddressMode.IMMEDIATE, 0x69);
        register(ADC, AddressMode.ZPG, 0x65);
        register(ADC, AddressMode.ZPG_X, 0x75);
        register(ADC, AddressMode.ABSOLUTE, 0x6d);
        register(ADC, AddressMode.ABSOLUTE_X, 0x7d);
        register(ADC, AddressMode.ABSOLUTE_Y, 0x79);
        register(ADC, AddressMode.X_INDIRECT, 0x61);
        register(ADC, AddressMode.INDIRECT_Y, 0x71);
        register(AND, AddressMode.IMMEDIATE, 0x29);
        register(AND, AddressMode.ZPG, 0x25);
        register(AND, AddressMode.ZPG_X, 0x35);
        register(AND, AddressMode.ABSOLUTE, 0x2d);
        register(AND, AddressMode.ABSOLUTE_X, 0x3d);
        register(AND, AddressMode.ABSOLUTE_Y, 0x39);
        register(AND, AddressMode.X_INDIRECT, 0x21);
        register(AND, AddressMode.INDIRECT_Y, 0x31);
        register(ASL, AddressMode.ACCUMULATOR, 0xa);
        register(ASL, AddressMode.ZPG, 0x6);
        register(ASL, AddressMode.ZPG_X, 0x16);
        register(ASL, AddressMode.ABSOLUTE, 0xe);
        register(ASL, AddressMode.ABSOLUTE_X, 0x1e);
        register(BCC, AddressMode.RELATIVE, 0x90);
        register(BCS, AddressMode.RELATIVE, 0xb0);
        register(BEQ, AddressMode.RELATIVE, 0xf0);
        register(BIT, AddressMode.ZPG, 0x24);
        register(BIT, AddressMode.ABSOLUTE, 0x2c);
        register(BMI, AddressMode.RELATIVE, 0x30);
        register(BNE, AddressMode.RELATIVE, 0xd0);
        register(BPL, AddressMode.RELATIVE, 0x10);
        register(BRK, AddressMode.IMPLIED, 0x0);
        register(BVC, AddressMode.RELATIVE, 0x50);
        register(BVS, AddressMode.RELATIVE, 0x70);
        register(CLC, AddressMode.IMPLIED, 0x18);
        register(CLD, AddressMode.IMPLIED, 0xd8);
        register(CLI, AddressMode.IMPLIED, 0x58);
        register(CLV, AddressMode.IMPLIED, 0xb8);
        register(CMP, AddressMode.IMMEDIATE, 0xc9);
        register(CMP, AddressMode.ZPG, 0xc5);
        register(CMP, AddressMode.ZPG_X, 0xd5);
        register(CMP, AddressMode.ABSOLUTE, 0xcd);
        register(CMP, AddressMode.ABSOLUTE_X, 0xdd);
        register(CMP, AddressMode.ABSOLUTE_Y, 0xd9);
        register(CMP, AddressMode.X_INDIRECT, 0xc1);
        register(CMP, AddressMode.INDIRECT_Y, 0xd1);
        register(CPX, AddressMode.IMMEDIATE, 0xe0);
        register(CPX, AddressMode.ZPG, 0xe4);
        register(CPX, AddressMode.ABSOLUTE, 0xec);
        register(CPY, AddressMode.IMMEDIATE, 0xc0);
        register(CPY, AddressMode.ZPG, 0xc4);
        register(CPY, AddressMode.ABSOLUTE, 0xcc);
        register(DEC, AddressMode.ZPG, 0xc6);
        register(DEC, AddressMode.ZPG_X, 0xd6);
        register(DEC, AddressMode.ABSOLUTE, 0xce);
        register(DEC, AddressMode.ABSOLUTE_X, 0xde);
        register(DEX, AddressMode.IMPLIED, 0xca);
        register(DEY, AddressMode.IMPLIED, 0x88);
        register(EOR, AddressMode.IMMEDIATE, 0x49);
        register(EOR, AddressMode.ZPG, 0x45);
        register(EOR, AddressMode.ZPG_X, 0x55);
        register(EOR, AddressMode.ABSOLUTE, 0x4d);
        register(EOR, AddressMode.ABSOLUTE_X, 0x5d);
        register(EOR, AddressMode.ABSOLUTE_Y, 0x59);
        register(EOR, AddressMode.X_INDIRECT, 0x41);
        register(EOR, AddressMode.INDIRECT_Y, 0x51);
        register(INC, AddressMode.ZPG, 0xe6);
        register(INC, AddressMode.ZPG_X, 0xf6);
        register(INC, AddressMode.ABSOLUTE, 0xee);
        register(INC, AddressMode.ABSOLUTE_X, 0xfe);
        register(INX, AddressMode.IMPLIED, 0xe8);
        register(INY, AddressMode.IMPLIED, 0xc8);
        register(JMP, AddressMode.ABSOLUTE, 0x4c);
        register(JMP, AddressMode.INDIRECT, 0x6c);
        register(JSR, AddressMode.ABSOLUTE, 0x20);
        register(LDA, AddressMode.IMMEDIATE, 0xa9);
        register(LDA, AddressMode.ZPG, 0xa5);
        register(LDA, AddressMode.ZPG_X, 0xb5);
        register(LDA, AddressMode.ABSOLUTE, 0xad);
        register(LDA, AddressMode.ABSOLUTE_X, 0xbd);
        register(LDA, AddressMode.ABSOLUTE_Y, 0xb9);
        register(LDA, AddressMode.X_INDIRECT, 0xa1);
        register(LDA, AddressMode.INDIRECT_Y, 0xb1);
        register(LDX, AddressMode.IMMEDIATE, 0xa2);
        register(LDX, AddressMode.ZPG, 0xa6);
        register(LDX, AddressMode.ZPG_Y, 0xb6);
        register(LDX, AddressMode.ABSOLUTE, 0xae);
        register(LDX, AddressMode.ABSOLUTE_Y, 0xbe);
        register(LDY, AddressMode.IMMEDIATE, 0xa0);
        register(LDY, AddressMode.ZPG, 0xa4);
        register(LDY, AddressMode.ZPG_X, 0xb4);
        register(LDY, AddressMode.ABSOLUTE, 0xac);
        register(LDY, AddressMode.ABSOLUTE_X, 0xbc);
        register(LSR, AddressMode.ACCUMULATOR, 0x4a);
        register(LSR, AddressMode.ZPG, 0x46);
        register(LSR, AddressMode.ZPG_X, 0x56);
        register(LSR, AddressMode.ABSOLUTE, 0x4e);
        register(LSR, AddressMode.ABSOLUTE_X, 0x5e);
        register(NOP, AddressMode.IMPLIED, 0xea);
        register(ORA, AddressMode.IMMEDIATE, 0x9);
        register(ORA, AddressMode.ZPG, 0x5);
        register(ORA, AddressMode.ZPG_X, 0x15);
        register(ORA, AddressMode.ABSOLUTE, 0xd);
        register(ORA, AddressMode.ABSOLUTE_X, 0x1d);
        register(ORA, AddressMode.ABSOLUTE_Y, 0x19);
        register(ORA, AddressMode.X_INDIRECT, 0x1);
        register(ORA, AddressMode.INDIRECT_Y, 0x11);
        register(PHA, AddressMode.IMPLIED, 0x48);
        register(PHP, AddressMode.IMPLIED, 0x8);
        register(PLA, AddressMode.IMPLIED, 0x68);
        register(PLP, AddressMode.IMPLIED, 0x28);
        register(ROL, AddressMode.ACCUMULATOR, 0x2a);
        register(ROL, AddressMode.ZPG, 0x26);
        register(ROL, AddressMode.ZPG_X, 0x36);
        register(ROL, AddressMode.ABSOLUTE, 0x2e);
        register(ROL, AddressMode.ABSOLUTE_X, 0x3e);
        register(ROR, AddressMode.ACCUMULATOR, 0x6a);
        register(ROR, AddressMode.ZPG, 0x66);
        register(ROR, AddressMode.ZPG_X, 0x76);
        register(ROR, AddressMode.ABSOLUTE, 0x6e);
        register(ROR, AddressMode.ABSOLUTE_X, 0x7e);
        register(RTI, AddressMode.IMPLIED, 0x40);
        register(RTS, AddressMode.IMPLIED, RTS_OPCODE);
        register(SBC, AddressMode.IMMEDIATE, 0xe9);
        register(SBC, AddressMode.ZPG, 0xe5);
        register(SBC, AddressMode.ZPG_X, 0xf5);
        register(SBC, AddressMode.ABSOLUTE, 0xed);
        register(SBC, AddressMode.ABSOLUTE_X, 0xfd);
        register(SBC, AddressMode.ABSOLUTE_Y, 0xf9);
        register(SBC, AddressMode.X_INDIRECT, 0xe1);
        register(SBC, AddressMode.INDIRECT_Y, 0xf1);
        register(SEC, AddressMode.IMPLIED, 0x38);
        register(SED, AddressMode.IMPLIED, 0xf8);
        register(SEI, AddressMode.IMPLIED, 0x78);
        register(STA, AddressMode.ZPG, 0x85);
        register(STA, AddressMode.ZPG_X, 0x95);
        register(STA, AddressMode.ABSOLUTE, 0x8d);
        register(STA, AddressMode.ABSOLUTE_X, 0x9d);
        register(STA, AddressMode.ABSOLUTE_Y, 0x99);
        register(STA, AddressMode.X_INDIRECT, 0x81);
        register(STA, AddressMode.INDIRECT_Y, 0x91);
        register(STX, AddressMode.ZPG, 0x86);
        register(STX, AddressMode.ZPG_Y, 0x96);
        register(STX, AddressMode.ABSOLUTE, 0x8e);
        register(STY, AddressMode.ZPG, 0x84);
        register(STY, AddressMode.ZPG_X, 0x94);
        register(STY, AddressMode.ABSOLUTE, 0x8c);
        register(TAX, AddressMode.IMPLIED, 0xaa);
        register(TAY, AddressMode.IMPLIED, 0xa8);
        register(TSX, AddressMode.IMPLIED, 0xba);
        register(TXA, AddressMode.IMPLIED, 0x8a);
        register(TXS, AddressMode.IMPLIED, 0x9a);
        register(TYA, AddressMode.IMPLIED, 0x98);
    }

    private void register(final Instruction instruction, final AddressMode addressMode, final int opCode) {
        final InstructionKey key = new InstructionKey(instruction, addressMode);
        if (instructionToCode.containsKey(key)) {
            throw new IllegalStateException(key + ": duplicate instruction key");
        }
        instructionToCode.put(key, opCode);
        if (codeToInstruction[opCode] != null) {
            throw new IllegalStateException("0x" + Integer.toHexString(opCode) + ": duplicate opcode");
        }
        instructionSupportedAddressModes.computeIfAbsent(instruction, k -> EnumSet.noneOf(AddressMode.class)).add(addressMode);
        addressModeToInstructions.computeIfAbsent(addressMode, k -> EnumSet.noneOf(Instruction.class)).add(instruction);
        codeToInstruction[opCode] = key;
    }

    public InstructionKey decode(final int opcode) {
        final InstructionKey ret = codeToInstruction[opcode];
        if (ret == null) {
            throw new IllegalStateException("0x" + Integer.toHexString(opcode) + ": unrecognised opcode");
        }
        return ret;
    }

    public List<Integer> encode(final Instruction instruction, final AddressMode addressMode, final int parm) {
        final InstructionKey key = new InstructionKey(instruction, addressMode);
        if (!instructionToCode.containsKey(key)) {
            throw new IllegalStateException("Could not decode " + instruction);
        }

        final List<Integer> ret = new ArrayList<>();

        ret.add(instructionToCode.get(key));

        final int parameterByteCount = key.getAddressMode().getParameterByteCount();
        if (parameterByteCount == 1) {
            ret.add(Util.checkUnsignedByte(parm));
        } else if (parameterByteCount == 2) {
            Util.checkUnsignedWord(parm);
            ret.add(parm & 0xff);
            ret.add((parm >>> 8) & 0xff);
        }
        return ret;
    }

    public boolean isAddressModeSupported(final Instruction instruction, final AddressMode addressMode) {
        return instructionSupportedAddressModes.containsKey(instruction) && instructionSupportedAddressModes.get(instruction).contains(addressMode);
    }

    public Set<AddressMode> getSupportedAddressModes(final Instruction instruction) {
        if (!instructionSupportedAddressModes.containsKey(instruction)) {
            return Collections.emptySet();
        } else {
            return EnumSet.copyOf(instructionSupportedAddressModes.get(instruction));
        }
    }

    public void printAddressModeToInstruction() {
        for (AddressMode am : addressModeToInstructions.keySet()) {
            System.out.println(am);
            final Set<Instruction> instructions = new TreeSet<>(Comparator.comparing(Enum::name));
            instructions.addAll(addressModeToInstructions.get(am));
            instructions.forEach(i -> System.out.println("    " + i));
        }
    }
}
