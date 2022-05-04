package com.jbeeb.memory;

import com.jbeeb.cpu.Cpu;
import com.jbeeb.cpu.Flag;

import java.util.Objects;

public final class AtomicFetchIntercept implements FetchIntercept {

    private final Cpu cpu;
    private final Runnable runnable;

    public AtomicFetchIntercept(Cpu cpu, Runnable runnable) {
        this.cpu = Objects.requireNonNull(cpu);
        this.runnable = Objects.requireNonNull(runnable);
    }

    @Override
    public boolean run() {
        final boolean interruptIn = cpu.isFlagSet(Flag.INTERRUPT);
        cpu.setFlag(Flag.INTERRUPT, true);
        try {
            runnable.run();
            return false;
        } finally {
            cpu.setFlag(Flag.INTERRUPT, interruptIn);
        }
    }
}
