package com.jbeeb.main;

import com.jbeeb.util.*;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public final class JavaBeeb {

    public static final File HOME = new File(System.getProperty("user.home"), "jbeeb");
    public static final File ROMS = new File(HOME, "roms");
    public static final File STATE = new File(HOME, "state");
    public static final File FILES = new File(HOME, "files");

    static {
        HOME.mkdirs();
        ROMS.mkdirs();
        STATE.mkdirs();
        FILES.mkdirs();
    }

    public static final BBCFile SNOWBALL = new BBCFile("SNOW2", 0x1180, 0x7320);
    public static final BBCFile CHUCKIE_EGG = new BBCFile("CH_EGG", 0x1100, 0x29AB);
    public static final BBCFile ARCADIANS = new BBCFile("Arcadia", 0x1900, 0x3F00);
    public static final BBCFile SNAPPER = new BBCFile("SNAP2", 0x1900, 0x1900);
    public static final BBCFile ROCKET_RAID = new BBCFile("RAIDOBJ", 0xE00, 0xE00);
    public static final BBCFile METEORS = new BBCFile("Meteor2", 0xE00, 0xE00);
    public static final BBCFile MAGIC_MUSHROOMS = new BBCFile("MM-GAME", 0x2000, 0x4700);
    public static final BBCFile SENTINEL = new BBCFile("Sentnel", 0x1900, 0x6D00);
    public static final BBCFile FREEFALL = new BBCFile("FREEFA3", 0xE00, 0x5080); // Keys don't work
    public static final BBCFile CASTLE_QUEST = new BBCFile("BCSTLQST", 0x1900, 0x1900);
    public static final BBCFile LABYRINTH = new BBCFile("LABYRIN", 0x1100, 0x624E); // Screem doesn't look correct
    public static final BBCFile FRAK = new BBCFile("FRAK", 0x1900, 0x8023);
    public static final BBCFile FRAK2 = new BBCFile("FRAK2", 0x204C, 0x2640);
    public static final BBCFile FRAK3 = new BBCFile("FRAK3", 0x2F00, 0x237D);
    public static final BBCFile FRAKSCR = new BBCFile("FRAKSCR", 0x7C00, 0x0000);
    public static final BBCFile SPACE_INVADERS = new BBCFile("Vads2", 0x1900, 0x1900);
    public static final BBCFile THRUST = new BBCFile("THRUST3", 0x1A00, 0x3D6E); // Unrecognized opcode

    private static final BBCFile FILE_TO_RUN = CHUCKIE_EGG;

    public static void main(final String[] args) throws Exception {
        createAndRunBBC();
    }

    private static final NumberFormat FMT = new DecimalFormat("0.00");

    private static void createAndRunBBC() throws Exception {

        final BBCMicro bbc = new BBCMicro();
        final long startTime = System.nanoTime();
        final Thread t = new Thread(() -> {
            if (FILE_TO_RUN != null) {
                try {
                    Thread.sleep(2000);
                    FILE_TO_RUN.run(bbc.getCpu(), bbc.getRam());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            try {
                while (true) {
                    Thread.sleep(5000);
                    updateSystemStatus(bbc.getSystemStatus(), bbc.getCpu().getCycleCount(), (System.nanoTime() - startTime));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
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
