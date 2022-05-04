package com.jbeeb.cpu;

import com.jbeeb.memory.Memory;

public final class CpuUtil {

    public static void osfsc(final Memory memory, final Cpu cpu, final int a) {
        final int addr = memory.readWord(0x21e);
        cpu.setA(a, true);
        cpu.JSR(addr);
    }

    public static void oswrch(final Cpu cpu, final int ch) {
        cpu.setA(ch, true);
        cpu.JSR(0xFFEE);
    }

    public static void osnewl(final Cpu cpu) {
        cpu.JSR(0xFFE7);
    }

    public static void osbyte(final Cpu cpu, final int a, final int x, final int y) {
        cpu.setA(a, true);
        cpu.setX(x, true);
        cpu.setY(y, true);
        cpu.JSR(0xFFF4);
    }

    public static void print(final Cpu cpu, final String s) {
        print(cpu, s, false);
    }

    public static void println(final Cpu cpu, final String s) {
        print(cpu, s, true);
    }

    private static void print(final Cpu cpu, final String s, final boolean newline) {
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            oswrch(cpu, (c >= 32 && c <= 127) ? (int) c : 32);
        }
        if (newline) {
            osnewl(cpu);
        }
    }
}
