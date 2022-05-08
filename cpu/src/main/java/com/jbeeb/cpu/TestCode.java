package com.jbeeb.cpu;

import java.util.*;
import java.util.function.Predicate;

public enum TestCode {
    CS(cpu -> cpu.isFlagSet(Flag.CARRY)),
    CC(cpu -> cpu.isFlagClear(Flag.CARRY)),
    ZS(cpu -> cpu.isFlagSet(Flag.ZERO), "EQ"),
    ZC(cpu -> cpu.isFlagClear(Flag.ZERO), "NE"),
    VS(cpu -> cpu.isFlagSet(Flag.OVERFLOW)),
    VC(cpu -> cpu.isFlagClear(Flag.OVERFLOW)),
    DS(cpu -> cpu.isFlagSet(Flag.DECIMAL)),
    DC(cpu -> cpu.isFlagClear(Flag.DECIMAL)),
    IS(cpu -> cpu.isFlagSet(Flag.INTERRUPT)),
    IC(cpu -> cpu.isFlagClear(Flag.INTERRUPT)),
    NS(cpu -> cpu.isFlagSet(Flag.INTERRUPT), "MI"),
    NC(cpu -> cpu.isFlagClear(Flag.INTERRUPT), "PL"),
    ;

    private static final TestCode[] VALUES;
    static {
        final TestCode[] values = values();
        VALUES = Arrays.copyOf(values, values.length);
    }

    private final Predicate<Cpu> test;
    private final Set<String> names = new HashSet<>();

    TestCode(final Predicate<Cpu> test, String... synonyms) {
        this.test = Objects.requireNonNull(test);
        this.names.add(name());
        this.names.addAll(Arrays.asList(synonyms));
    }

    public boolean test(final Cpu cpu) {
        return this.test.test(cpu);
    }

    public static TestCode get(final int code) {
        return VALUES[code];
    }

    public static Optional<TestCode> of(final String name) {
        final String n = name.toUpperCase();
        return Arrays.stream(VALUES).filter(c -> c.names.contains(n)).findFirst();
    }
}
