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

    public static String readStringIndirect(final Memory memory, final int address, final int offset) {
        int a = memory.readWord(address) + offset;
        return readStringAbsolute(memory, a);
    }

    public static String readStringAbsolute(final Memory memory, int a) {
        final StringBuilder s = new StringBuilder();
        int v;
        while ((v = memory.readByte(a++)) != 0xD) {
            s.append((char) v);
        }
        return s.toString();
    }

    public static void writeExtendedVector(
            final Memory memory,
            final Cpu cpu,
            final int vector,
            final int addr,
            final int romNumber
    ) {
        final int n = (vector - 0x200) / 2;
        final int a = 0xFF00 + 3 * n;
        memory.writeByte(vector, (a & 0xFF));
        memory.writeByte(vector + 1, ((a >>> 8) & 0xFF));
        osbyte(cpu, 0xa8, 0x00, 0xFF);
        final int v = (cpu.getX() & 0xFF) | ((cpu.getY() & 0xFF) << 8);
        final int va = v + 3 * n;
        memory.writeByte(va, addr & 0xFF);
        memory.writeByte(va + 1, (addr >>> 8) & 0xFF);
        memory.writeByte(va + 2, romNumber & 0xFF);
    }

    public static void badCommand(final Cpu cpu) {
        newlineMessage(cpu, "Bad command");
    }

    public static void newlineMessage(final Cpu cpu, final String message) {
        osnewl(cpu);
        message(cpu, message);
    }

    public static void message(final Cpu cpu, final String message) {
        println(cpu, message);
    }
}
