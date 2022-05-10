package com.jbeeb.cpu;

import com.jbeeb.util.Util;

public enum Instruction {
    //
    // Official instructions
    //
    ADC(InstructionType.READ) {
        @Override
        public void acceptValue(final Cpu cpu, final int value) {
            final int result = cpu.isFlagSet(Flag.DECIMAL) ?
                    Util.addWithCarryBCD(cpu, cpu.getA(), value, cpu.isFlagSet(Flag.CARRY)) :
                    Util.addWithCarry(cpu, cpu.getA(), value, cpu.isFlagSet(Flag.CARRY));
            cpu.setA(result, true);
        }
    },
    AND(InstructionType.READ) {
        @Override
        public void acceptValue(final Cpu cpu, final int value) {
            cpu.setA(cpu.getA() & value, true);
        }
    },
    ASL(InstructionType.READ_MODIFY_WRITE) {
        @Override
        public int transformValue(Cpu cpu, int value) {
            return Util.asl(cpu, value);
        }
    },
    BCC(InstructionType.BRANCH) {
        @Override
        public boolean branchCondition(Cpu cpu) {
            return cpu.isFlagClear(Flag.CARRY);
        }
    },
    BCS(InstructionType.BRANCH) {
        @Override
        public boolean branchCondition(Cpu cpu) {
            return cpu.isFlagSet(Flag.CARRY);
        }
    },
    BEQ(InstructionType.BRANCH) {
        @Override
        public boolean branchCondition(Cpu cpu) {
            return cpu.isFlagSet(Flag.ZERO);
        }
    },
    BIT(InstructionType.READ) {
        @Override
        public void acceptValue(final Cpu cpu, final int value) {
            final int a = cpu.getA();
            cpu.setFlag(Flag.NEGATIVE, (value & 0x80) != 0);
            cpu.setFlag(Flag.OVERFLOW, (value & 0x40) != 0);
            cpu.setFlag(Flag.ZERO, (value & a) == 0);
        }
    },
    BMI(InstructionType.BRANCH) {
        @Override
        public boolean branchCondition(Cpu cpu) {
            return cpu.isFlagSet(Flag.NEGATIVE);
        }
    },
    BNE(InstructionType.BRANCH) {
        @Override
        public boolean branchCondition(Cpu cpu) {
            return cpu.isFlagClear(Flag.ZERO);
        }
    },
    BPL(InstructionType.BRANCH) {
        @Override
        public boolean branchCondition(Cpu cpu) {
            return cpu.isFlagClear(Flag.NEGATIVE);
        }
    },
    BRK(InstructionType.STACK),
    BVC(InstructionType.BRANCH) {
        @Override
        public boolean branchCondition(Cpu cpu) {
            return cpu.isFlagClear(Flag.OVERFLOW);
        }
    },
    BVS(InstructionType.BRANCH) {
        @Override
        public boolean branchCondition(Cpu cpu) {
            return cpu.isFlagSet(Flag.OVERFLOW);
        }
    },
    CLC(InstructionType.IMPLIED) {
        @Override
        public void performImpliedAction(Cpu cpu) {
            cpu.clearFlag(Flag.CARRY);
        }
    },
    CLD(InstructionType.IMPLIED) {
        @Override
        public void performImpliedAction(Cpu cpu) {
            cpu.clearFlag(Flag.DECIMAL);
        }
    },
    CLI(InstructionType.IMPLIED) {
        @Override
        public void performImpliedAction(Cpu cpu) {
            cpu.clearFlag(Flag.INTERRUPT);
        }
    },
    CLV(InstructionType.IMPLIED) {
        @Override
        public void performImpliedAction(final Cpu cpu) {
            cpu.clearFlag(Flag.OVERFLOW);
        }
    },
    CMP(InstructionType.READ) {
        @Override
        public void acceptValue(final Cpu cpu, final int value) {
            Util.cmp(cpu, cpu.getA(), value);
        }
    },
    CPX(InstructionType.READ) {
        @Override
        public void acceptValue(final Cpu cpu, final int value) {
            Util.cmp(cpu, cpu.getX(), value);
        }
    },
    CPY(InstructionType.READ) {
        @Override
        public void acceptValue(final Cpu cpu, final int value) {
            Util.cmp(cpu, cpu.getY(), value);
        }
    },
    DEC(InstructionType.READ_MODIFY_WRITE) {
        @Override
        public int transformValue(Cpu cpu, int value) {
            return Util.dec(cpu, value);
        }
    },
    DEX(InstructionType.IMPLIED) {
        @Override
        public void performImpliedAction(final Cpu cpu) {
            cpu.setX((cpu.getX() - 1) & 0xFF, true);
        }
    },
    DEY(InstructionType.IMPLIED) {
        @Override
        public void performImpliedAction(final Cpu cpu) {
            cpu.setY((cpu.getY() - 1) & 0xFF, true);
        }
    },
    EOR(InstructionType.READ) {
        @Override
        public void acceptValue(final Cpu cpu, final int value) {
            cpu.setA(cpu.getA() ^ value, true);
        }
    },
    INC(InstructionType.READ_MODIFY_WRITE) {
        @Override
        public int transformValue(Cpu cpu, int value) {
            return Util.inc(cpu, value);
        }
    },
    INX(InstructionType.IMPLIED) {
        @Override
        public void performImpliedAction(final Cpu cpu) {
            cpu.setX((cpu.getX() + 1) & 0xFF, true);
        }
    },
    INY(InstructionType.IMPLIED) {
        @Override
        public void performImpliedAction(final Cpu cpu) {
            cpu.setY((cpu.getY() + 1) & 0xFF, true);
        }
    },
    JMP(InstructionType.JUMP),
    JSR(InstructionType.STACK),
    LDA(InstructionType.READ) {
        @Override
        public void acceptValue(final Cpu cpu, final int value) {
            cpu.setA(value, true);
        }
    },
    LDX(InstructionType.READ) {
        @Override
        public void acceptValue(final Cpu cpu, final int value) {
            cpu.setX(value, true);
        }
    },
    LDY(InstructionType.READ) {
        @Override
        public void acceptValue(final Cpu cpu, final int value) {
            cpu.setY(value, true);
        }
    },
    LSR(InstructionType.READ_MODIFY_WRITE) {
        @Override
        public int transformValue(Cpu cpu, int value) {
            return Util.lsr(cpu, value);
        }
    },
    NOP(InstructionType.IMPLIED) {
        @Override
        public void performImpliedAction(final Cpu cpu) {
            // Do nothing
        }
    },
    ORA(InstructionType.READ) {
        @Override
        public void acceptValue(final Cpu cpu, final int value) {
            cpu.setA(cpu.getA() | value, true);
        }
    },
    PHA(InstructionType.STACK),
    PHP(InstructionType.STACK),
    PLA(InstructionType.STACK),
    PLP(InstructionType.STACK),
    ROL(InstructionType.READ_MODIFY_WRITE) {
        @Override
        public int transformValue(Cpu cpu, int value) {
            return Util.rol(cpu, value, cpu.isFlagSet(Flag.CARRY));
        }
    },
    ROR(InstructionType.READ_MODIFY_WRITE) {
        @Override
        public int transformValue(Cpu cpu, int value) {
            return Util.ror(cpu, value, cpu.isFlagSet(Flag.CARRY));
        }
    },
    RTI(InstructionType.STACK),
    RTS(InstructionType.STACK),
    SBC(InstructionType.READ) {
        @Override
        public void acceptValue(final Cpu cpu, final int value) {
            final int result = cpu.isFlagSet(Flag.DECIMAL) ?
                    Util.subtractWithCarryBCD(cpu, cpu.getA(), value, cpu.isFlagSet(Flag.CARRY)) :
                    Util.subtractWithCarry(cpu, cpu.getA(), value, cpu.isFlagSet(Flag.CARRY));
            cpu.setA(result, true);
        }
    },
    SEC(InstructionType.IMPLIED) {
        @Override
        public void performImpliedAction(Cpu cpu) {
            cpu.setFlag(Flag.CARRY);
        }
    },
    SED(InstructionType.IMPLIED) {
        @Override
        public void performImpliedAction(Cpu cpu) {
            cpu.setFlag(Flag.DECIMAL);
        }
    },
    SEI(InstructionType.IMPLIED) {
        @Override
        public void performImpliedAction(Cpu cpu) {
            cpu.setFlag(Flag.INTERRUPT);
        }
    },
    STA(InstructionType.WRITE) {
        @Override
        public int readValue(Cpu cpu) {
            return cpu.getA();
        }
    },
    STX(InstructionType.WRITE) {
        @Override
        public int readValue(Cpu cpu) {
            return cpu.getX();
        }
    },
    STY(InstructionType.WRITE) {
        @Override
        public int readValue(Cpu cpu) {
            return cpu.getY();
        }
    },
    TAX(InstructionType.IMPLIED) {
        @Override
        public void performImpliedAction(Cpu cpu) {
            cpu.setX(cpu.getA(), true);
        }
    },
    TAY(InstructionType.IMPLIED) {
        @Override
        public void performImpliedAction(Cpu cpu) {
            cpu.setY(cpu.getA(), true);
        }
    },
    TSX(InstructionType.IMPLIED) {
        @Override
        public void performImpliedAction(Cpu cpu) {
            cpu.setX(cpu.getSP(), true);
        }
    },
    TXA(InstructionType.IMPLIED) {
        @Override
        public void performImpliedAction(Cpu cpu) {
            cpu.setA(cpu.getX(), true);
        }
    },
    TXS(InstructionType.IMPLIED) {
        @Override
        public void performImpliedAction(Cpu cpu) {
            cpu.setSP(cpu.getX());
        }
    },
    TYA(InstructionType.IMPLIED) {
        @Override
        public void performImpliedAction(Cpu cpu) {
            cpu.setA(cpu.getY(), true);
        }
    },

    HLT(InstructionType.IMPLIED) {
        @Override
        public void performImpliedAction(Cpu cpu) {
            cpu.halt(0);
        }
    },
    ERR(InstructionType.READ) {
        @Override
        public void acceptValue(Cpu cpu, int value) {
            cpu.halt(value);
        }
    },
    TST(InstructionType.READ) {
        @Override
        public void acceptValue(Cpu cpu, int value) {
            cpu.test(value);
        }
    },
    TRP(InstructionType.READ) {
        @Override
        public void acceptValue(Cpu cpu, int value) {
            cpu.trap(value);
        }
    },

    //
    // Illegal/undocumented instructions
    //
    ALR(InstructionType.READ) {
        @Override
        public void acceptValue(Cpu cpu, int value) {
            cpu.setA(cpu.getA() & value, true);
            cpu.setA(Util.lsr(cpu, cpu.getA()), true);
        }
    },
    ANC(InstructionType.READ) {
        @Override
        public void acceptValue(Cpu cpu, int value) {
            cpu.setA(cpu.getA() & value, true);
            cpu.setFlag(Flag.CARRY, cpu.isFlagSet(Flag.NEGATIVE));
        }
    },
    LAX(InstructionType.READ) {
        @Override
        public void acceptValue(Cpu cpu, int value) {
            cpu.setA(value, true);
            cpu.setX(value, true);
        }
    },
    RLA(InstructionType.READ_MODIFY_WRITE) {
        @Override
        public int transformValue(Cpu cpu, int value) {
            final int ret = Util.rol(cpu, value, cpu.isFlagSet(Flag.CARRY));
            cpu.setA(cpu.getA() & value, true);
            return ret;
        }
    }
    ;



    private final InstructionType type;

    Instruction(final InstructionType type) {
        this.type = type;
    }

    public InstructionType getType() {
        return type;
    }

    public int readValue(final Cpu cpu) {
        throw new UnsupportedOperationException();
    }

    public boolean branchCondition(final Cpu cpu) {
        throw new UnsupportedOperationException();
    }

    public void acceptValue(final Cpu cpu, final int value) {
        throw new UnsupportedOperationException();
    }

    public int transformValue(final Cpu cpu, final int value) {
        throw new UnsupportedOperationException();
    }

    public void performImpliedAction(Cpu cpu) {
        throw new UnsupportedOperationException();
    }
}
