package com.jbeeb.main;

import com.jbeeb.SheilaMemoryMappedDevice;
import com.jbeeb.cpu.Cpu;
import com.jbeeb.device.*;
import com.jbeeb.screen.Screen;
import com.jbeeb.memory.Memory;
import com.jbeeb.memory.ReadOnlyMemory;
import com.jbeeb.util.Runner;
import com.jbeeb.util.SystemStatus;
import com.jbeeb.util.SystemStatusImpl;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class JavaBeeb {

    private static final File OS_ROM_FILE = new File(System.getProperty("user.home"), "OS-1.2.rom");
    private static final File BASIC_ROM_FILE = new File(System.getProperty("user.home"), "BASIC2.rom");

    private static final int SHEILA = 0xFE00;

    public static void main(final String[] args) throws Exception {
        runMachine(Long.MAX_VALUE);
    }

    private static final NumberFormat FMT = new DecimalFormat("0.00");

    private static void runMachine(final long maxCycleCount) throws Exception {

        final SystemStatus systemStatus = new SystemStatusImpl();

        final VideoULA videoULA = new VideoULA(
                systemStatus,
                "Video ULA",
                SHEILA + 0x20
        );

        final SystemVIA systemVIA = new SystemVIA(
                systemStatus,
                "System VIA",
                SHEILA + 0x40, 32
        );

        final UserVIA userVIA = new UserVIA(
                systemStatus,
                "User VIA",
                SHEILA + 0x60,
                32
        );

        final CRTC6845 crtc6845 = new CRTC6845(
                systemStatus,
                "CRTC 6845",
                SHEILA,
                systemVIA
        );

        final List<MemoryMappedDevice> devices = new ArrayList<>();
        devices.add(videoULA);
        devices.add(systemVIA);
        devices.add(crtc6845);
        devices.add(userVIA);
        devices.add(new SheilaMemoryMappedDevice(systemStatus));

        final Memory languageRom = ReadOnlyMemory.fromFile(0x8000, BASIC_ROM_FILE);
        final Memory osRom = ReadOnlyMemory.fromFile(0xC000, OS_ROM_FILE);
        final Memory memory = Memory.bbcMicroB(devices, languageRom, osRom);

        final Screen screen = new Screen(
                systemStatus,
                memory,
                videoULA,
                crtc6845,
                systemVIA
        );
        screen.addKeyDownListener(systemVIA::keyDown);
        screen.addKeyUpListener(systemVIA::keyUp);
        crtc6845.addVSyncListener(screen::vsync);

        final Cpu cpu = new Cpu(systemStatus, memory);
        cpu.setVerboseSupplier(() -> false);

        final Runner runner = new Runner(
                2_000_000,
                maxCycleCount,
                Arrays.asList(cpu, systemVIA, crtc6845, screen)
        );
        final Machine machine = new Machine(runner);
        machine.addInterruptSource(crtc6845);
        machine.addInterruptSource(systemVIA);
        machine.addInterruptSource(userVIA);
        machine.addInterruptSource(videoULA);

        cpu.setInterruptSource(machine);
        final long startTime = System.nanoTime();
        final Thread t = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5000);
                    updateSystemStatus(systemStatus, cpu.getCycleCount(), (System.nanoTime() - startTime));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        t.start();
        machine.run(() -> false);
        reportCyclesPerSecond(cpu.getCycleCount(), System.nanoTime() - startTime);
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
