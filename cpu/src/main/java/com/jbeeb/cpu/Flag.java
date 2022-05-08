package com.jbeeb.cpu;

public enum Flag {

    CARRY("C"),
    ZERO("Z"),
    INTERRUPT("I"),
    DECIMAL("D"),
    BREAK("B"),
    RESERVED("R"),
    OVERFLOW("V"),
    NEGATIVE("N");

    final String code;

    Flag(final String code) {
        this.code = code;
    }

    private int bit() {
        return 1 << ordinal();
    }

    private int mask() {
        return (~bit()) & 0xFF;
    }

    public boolean isSet(final int flags) {
        return (flags & bit()) != 0;
    }

    public boolean isClear(final int flags) {
        return !isSet(flags);
    }

    public int set(final int flags) {
        return flags | bit();
    }

    public int clear(final int flags) {
        return flags & mask();
    }

    public int set(final int flags, boolean set) {
        return (set) ? set(flags) : clear(flags);
    }

    public static String toString(final int flags) {
        final Flag[] values = values();
        final StringBuilder s = new StringBuilder();
        for (int i = 7; i >= 0; i--) {
            String c = values[i].code;
            if ((flags & (1 << i)) == 0) {
                c = c.toLowerCase();
            }
            s.append(c);
        }
        return s.toString();
    }
}
