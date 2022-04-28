package com.jbeeb;

import com.jbeeb.util.Runner;
import com.jbeeb.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;

public final class Machine implements InterruptSource {

    private final List<InterruptSource> interruptSources = new ArrayList<>();
    private final Runner runner;

    public Machine(Runner runner) {
        this.runner = Objects.requireNonNull(runner);
    }

    public void run(final BooleanSupplier haltCondition) {
        this.runner.run(haltCondition);
    }

    public void addInterruptSource(final InterruptSource source) {
        this.interruptSources.add(source);
    }

    @Override
    public String getName() {
        return "machine";
    }

    @Override
    public boolean isIRQ() {
        for (InterruptSource s : interruptSources) {
            if (s.isIRQ()) {
                s.isIRQ();
                //Util.log(" irq source " + s.getClass().getSimpleName(), 0);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isNMI() {
        for (InterruptSource s : interruptSources) {
            if (s.isNMI()) {
                return true;
            }
        }
        return false;
    }
}
