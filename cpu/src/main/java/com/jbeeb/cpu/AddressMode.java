package com.jbeeb.cpu;

import com.jbeeb.util.Util;

public enum AddressMode {
    ACCUMULATOR(0) {
        @Override
        public String formatOperand(int pc, int operand) {
            return " A";
        }
    },
    ABSOLUTE(2) {
        @Override
        public String formatOperand(int pc, int operand) {
            return " " + Util.formatHexWord(operand);
        }
    },
    ABSOLUTE_X(2) {
        @Override
        public String formatOperand(int pc, int operand) {
            return " " + Util.formatHexWord(operand) + ",X";
        }
    },
    ABSOLUTE_Y(2) {
        @Override
        public String formatOperand(int pc, int operand) {
            return " " + Util.formatHexWord(operand) + ",X";
        }
    },
    IMMEDIATE(1) {
        @Override
        public String formatOperand(int pc, int operand) {
            return " #" + Util.formatHexByte(operand);
        }
    },
    IMPLIED(0) {
        @Override
        public String formatOperand(int pc, int operand) {
            return "";
        }
    },
    INDIRECT(2) {
        @Override
        public String formatOperand(int pc, int operand) {
            return " (" + Util.formatHexWord(operand) + ")";
        }
    },
    X_INDIRECT(1) {
        @Override
        public String formatOperand(int pc, int operand) {
            return " (" + Util.formatHexByte(operand) + ",X)";
        }
    },
    INDIRECT_Y(1) {
        @Override
        public String formatOperand(int pc, int operand) {
            return " (" + Util.formatHexByte(operand) + "),Y";
        }
    },
    RELATIVE(1) {
        @Override
        public String formatOperand(int pc, int operand) {
            return " " + Util.formatHexWord(pc + Util.signed(operand) + 1);
        }
    },
    ZPG(1) {
        @Override
        public String formatOperand(int pc, int operand) {
            return " " + Util.formatHexByte(operand);
        }
    },
    ZPG_X(1) {
        @Override
        public String formatOperand(int pc, int operand) {
            return " " + Util.formatHexByte(operand) + ",X";
        }
    },
    ZPG_Y(1) {
        @Override
        public String formatOperand(int pc, int operand) {
            return " " + Util.formatHexByte(operand) + ",Y";
        }
    };

    private final int parameterByteCount;

    AddressMode(final int parameterByteCount) {
        this.parameterByteCount = parameterByteCount;
    }

    public int getParameterByteCount() {
        return parameterByteCount;
    }

    public abstract String formatOperand(final int pc, final int operand);
}
