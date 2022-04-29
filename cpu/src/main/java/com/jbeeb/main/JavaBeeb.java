package com.jbeeb.main;

import com.jbeeb.util.*;

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
        final Thread t = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5000);
                    updateSystemStatus(bbc.getSystemStatus(), bbc.getCpu().getCycleCount(), (System.nanoTime() - startTime));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        t.start();
        bbc.run(() -> false);
        reportCyclesPerSecond(bbc.getCpu().getCycleCount(), System.nanoTime() - startTime);
    }

    private static void updateSystemStatus(final SystemStatus systemStatus, final long cycleCount, final long duration) {
        final double seconds = (double) duration / 1_000_000_000L;
        final double cyclesPerSecond = cycleCount / seconds / 1000000.0;
        systemStatus.putString(SystemStatus.KEY_MILLION_CYCLES_PER_SECOND, FMT.format(cyclesPerSecond));
        systemStatus.putLong(SystemStatus.KEY_TOTAL_CYCLES, cycleCount);
        systemStatus.putString(SystemStatus.KEY_UP_TIME, FMT.format(seconds));
    }

    private static void reportCyclesPerSecond(final long cycleCount, final long duration) {
        final double seconds = (double) duration / 1_000_000_000L;
        final double cyclesPerSecond = cycleCount / seconds / 1000000.0;
        System.err.println("cycles = " + cycleCount + " secs = " + FMT.format(seconds) + " mega-cps = " + FMT.format(cyclesPerSecond));
    }
}
