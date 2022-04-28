package com.jbeeb.main;

import com.jbeeb.Machine;
import com.jbeeb.SheilaMemoryMappedDevice;
import com.jbeeb.cpu.Cpu;
import com.jbeeb.device.*;
import com.jbeeb.display.Display;
import com.jbeeb.memory.Memory;
import com.jbeeb.memory.ReadOnlyMemory;
import com.jbeeb.util.Runner;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class JBeeb {

    private static final File OS_ROM_FILE = new File(System.getProperty("user.home"), "OS-1.2.rom");
    private static final File BASIC_ROM_FILE = new File(System.getProperty("user.home"), "BASIC2.rom");

    private static final int SHEILA = 0xFE00;

    public static void main(final String[] args) throws Exception {
        runMachine(Long.MAX_VALUE, false);
    }

    private static final NumberFormat FMT = new DecimalFormat("0.00");

    private static void runMachine(final long maxCycleCount, final boolean verbose) throws Exception {
        final VideoULA videoULA = new VideoULA("Video ULA", SHEILA + 0x20);
        final SystemVIA systemVIA = new SystemVIA("System VIA", SHEILA + 0x40, 32);
        final UserVIA userVIA = new UserVIA("User VIA", SHEILA + 0x60, 32);
        final CRTC6845 crtc6845 = new CRTC6845("CRTC 6845", SHEILA + 0x00, systemVIA);

        final List<MemoryMappedDevice> devices = new ArrayList<>();
        devices.add(videoULA);
        devices.add(systemVIA);
        devices.add(crtc6845);
        //devices.add(userVIA);
        devices.add(new SheilaMemoryMappedDevice());

        final Memory languageRom = ReadOnlyMemory.fromFile(0x8000, BASIC_ROM_FILE);
        final Memory osRom = ReadOnlyMemory.fromFile(0xC000, OS_ROM_FILE);
        final Memory memory = Memory.bbcMicroB(devices, languageRom, osRom);

        final Display display = new Display(memory, videoULA, crtc6845, systemVIA);
        display.addKeyDownListener((c,s) -> systemVIA.keyDown(c, s));
        display.addKeyUpListener(systemVIA::keyUp);
        crtc6845.addVSyncListener(display::vsync);

        final Cpu cpu = new Cpu(memory);
        cpu.setVerboseSupplier(() -> false);

        final Runner runner = new Runner(
                2_000_000,
                maxCycleCount,
                Arrays.asList(cpu, systemVIA, crtc6845)
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
                    reportCyclesPerSecond(cpu.getCycleCount(), (System.nanoTime() - startTime));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        t.start();
        machine.run(() -> false);
        reportCyclesPerSecond(cpu.getCycleCount(), System.nanoTime() - startTime);
    }

    private static void reportCyclesPerSecond(final long cycleCount, final long duration) {
        final double seconds = (double) duration / 1_000_000_000L;
        final double cyclesPerSecond = cycleCount / seconds / 1000000.0;
        System.err.println("cycles = " + cycleCount + " secs = " + FMT.format(seconds) + " mega-cps = " + FMT.format(cyclesPerSecond));
    }
}
