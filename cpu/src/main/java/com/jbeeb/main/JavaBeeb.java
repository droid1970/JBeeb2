package com.jbeeb.main;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public final class JavaBeeb {

    public static void main(final String[] args) throws Exception {
        createAndRunBBC();
    }

    private static final NumberFormat FMT = new DecimalFormat("0.00");

    private static void createAndRunBBC() throws Exception {
        final BBCMicro bbc = new BBCMicro();
        final long startTime = System.nanoTime();
        bbc.run(() -> false);
        reportCyclesPerSecond(bbc.getCpu().getCycleCount(), System.nanoTime() - startTime);
    }

    private static void reportCyclesPerSecond(final long cycleCount, final long duration) {
        final double seconds = (double) duration / 1_000_000_000L;
        final double cyclesPerSecond = cycleCount / seconds / 1000000.0;
        System.err.println("cycles = " + cycleCount + " secs = " + FMT.format(seconds) + " mega-cps = " + FMT.format(cyclesPerSecond));
    }
}
