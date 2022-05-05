package com.jbeeb.main;

import com.jbeeb.assembler.Disassembler;
import com.jbeeb.cpu.Cpu;
import com.jbeeb.cpu.Flag;
import com.jbeeb.cpu.InstructionSet;
import com.jbeeb.device.*;
import com.jbeeb.disk.FloppyDiskController;
import com.jbeeb.localfs.FilingSystemROM;
import com.jbeeb.memory.*;
import com.jbeeb.screen.Screen;
import com.jbeeb.util.*;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public final class BBCMicro implements InterruptSource {

    private static final boolean INSTALL_DFS = false;

    private static final File STATE_FILE = new File(System.getProperty("user.home"), "state.bbc");
    private static final String BASIC_ROM_RESOURCE_NAME = "/roms/BASIC2.rom";
    private static final String DFS_ROM_RESOURCE_NAME = "/roms/DFS-1.2.rom";
    private static final String OS_ROM_RESOURCE_NAME = "/roms/OS-1.2.rom";

    private static final int SHEILA = 0xFE00;

    private final List<InterruptSource> interruptSources = new ArrayList<>();
    private final SystemStatus systemStatus;

    private final VideoULA videoULA;
    private final SystemVIA systemVIA;
    private final UserVIA userVIA;
    private final Crtc6845 crtc6845;
    private final FloppyDiskController fdc;
    private final PagedRomSelect pagedRomSelect;
    private final RandomAccessMemory ram;

    private final Cpu cpu;

    private final Runner runner;

    public SystemStatus getSystemStatus() {
        return systemStatus;
    }

    public Cpu getCpu() {
        return cpu;
    }

    public Memory getRam() {
        return ram;
    }

    public SystemVIA getSystemVIA() {
        return systemVIA;
    }

    public BBCMicro() throws Exception {

        this.systemStatus = new SystemStatusImpl();

        this.videoULA = new VideoULA(
                systemStatus,
                "Video ULA",
                SHEILA + 0x20
        );

        this.systemVIA = new SystemVIA(
                systemStatus,
                "System VIA",
                SHEILA + 0x40, 32
        );

        this.userVIA = new UserVIA(
                systemStatus,
                "User VIA",
                SHEILA + 0x60,
                32
        );

        this.crtc6845 = new Crtc6845(
                systemStatus,
                "CRTC 6845",
                SHEILA,
                systemVIA
        );

        final Scheduler scheduler = new DefaultScheduler();
        this.fdc = (INSTALL_DFS) ? new FloppyDiskController(systemStatus, scheduler, "FDC8271", SHEILA + 0x80) : null;

        this.pagedRomSelect = new PagedRomSelect(systemStatus, "Paged ROM", SHEILA + 0x30, 1);

        final List<MemoryMappedDevice> devices = new ArrayList<>();
        devices.add(videoULA);
        devices.add(systemVIA);
        devices.add(crtc6845);
        devices.add(userVIA);
        if (fdc != null) {
            devices.add(fdc);
        }
        devices.add(pagedRomSelect);
        devices.add(new SheilaMemoryMappedDevice(systemStatus));

        if (fdc != null) {
            fdc.load(0, new File(System.getProperty("user.home"), "Arcadians.ssd"));
            fdc.load(1, new File(System.getProperty("user.home"), "Arcadians.ssd"));
        }

        final ReadOnlyMemory basicRom = ReadOnlyMemory.fromResource(0x8000, BASIC_ROM_RESOURCE_NAME);
        final ReadOnlyMemory dfsRom = ReadOnlyMemory.fromResource(0x8000, DFS_ROM_RESOURCE_NAME);
        final FilingSystemROM filingSystemROM = new FilingSystemROM("My DFS", "(C) Ian T 2022");
        final Map<Integer, ReadOnlyMemory> roms = new HashMap<>();
        roms.put(15, basicRom);

        if (INSTALL_DFS) {
            roms.put(12, dfsRom);
        } else {
            roms.put(12, filingSystemROM);
        }

        final PagedROM pagedROM = new PagedROM(0x8000, 16384, pagedRomSelect, roms);
        final Memory osRom = ReadOnlyMemory.fromResource(0xC000, OS_ROM_RESOURCE_NAME);
        this.ram = new RandomAccessMemory(0, 32768);

        final Memory memory = Memory.bbcMicroB(devices, ram, pagedROM, osRom);

        final Screen screen = new Screen(
                systemStatus,
                this,
                memory,
                videoULA,
                crtc6845,
                systemVIA
        );
        screen.addKeyDownListener(systemVIA::keyDown);
        screen.addKeyUpListener(systemVIA::keyUp);
        crtc6845.addVSyncListener(screen::vsync);

        this.cpu = new Cpu(systemStatus, scheduler, memory);
//        testRom.installIntercept(0x9000, new AtomicFetchIntercept(cpu, () -> {
//            System.err.println("Test ROM entered: A = " + cpu.getA() + " X = " + cpu.getX() + " Y = " + cpu.getY());
//            switch (cpu.getA()) {
//                case 9:
//                    int addr = memory.readWord(0xF2) + cpu.getY();
//                    StringBuilder s = new StringBuilder();
//                    while (memory.readByte(addr) != 0xd) {
//                        s.append((char) memory.readByte(addr++));
//                    }
//                    System.err.println("*HELP " + s);
//                    break;
//            }
//            if (cpu.getA() == 1) {
//                cpu.setY(cpu.getY() + 1, true);
//            }
//        }));
        filingSystemROM.initialise(memory, cpu);
        cpu.setVerboseSupplier(() -> false);
        if (fdc != null) {
            this.fdc.setCpu(cpu);
        }

        this.runner = new Runner(
                systemStatus,
                2_000_000,
                Long.MAX_VALUE,
                Arrays.asList(cpu, systemVIA, userVIA, crtc6845, screen)
        );
        addInterruptSource(crtc6845);
        addInterruptSource(systemVIA);
        addInterruptSource(userVIA);
        addInterruptSource(videoULA);
        if (fdc != null) {
            addInterruptSource(fdc);
        }
        cpu.setInterruptSource(this);
//        scheduler.newTask(() -> {
//            System.err.println("INSV = " + Util.formatHexWord(memory.readWord(0x22a)));
//        }).schedule(2_000_000);
//        scheduler.newTask(() -> {
//            try {
//                final Disassembler dis = new Disassembler(new InstructionSet(), memory);
//                dis.setPC(0xfff4);
//                for (int i = 0; i < 10; i++) {
//                    System.err.println(dis.disassemble());
//                }
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        }).schedule(2_000_000);

        final int KEYV = 0xEF02;
        final int INSV = 0xE4B3;

        if (true) memory.installIntercept(INSV, new AtomicFetchIntercept(cpu, () -> {
            System.err.println("INSV: A = " + cpu.getA() + " X = " + cpu.getX());
//            final boolean C = cpu.isFlagSet(Flag.CARRY);
//            final boolean V = cpu.isFlagSet(Flag.OVERFLOW);
//            if (!C && !V) {
//                final int retLO = cpu.peekByte(7);
//                final int retHI = cpu.peekByte(8);
//                final int ret = (retLO & 0xFF) | ((retHI & 0xFF) << 8);
//                for (int i = 0; i < 10; i++) {
//                    System.err.println("stack " + i + " = " + Util.formatHexByte(cpu.peekByte(i)));
//                }
//                //cpu.onReturnTo(ret, () -> System.err.println("KEYV - returned with SHIFT pressed = " + cpu.isFlagSet(Flag.NEGATIVE) + " CTRL pressed = " + cpu.isFlagSet(Flag.OVERFLOW)));
////                final int retLO = cpu.peekByte(0);
////                final int retHI = cpu.peekByte(1);
////                final int ret = (retLO & 0xFF) | ((retHI & 0xFF) << 8);
////                cpu.onReturnTo(ret  - 2, () -> System.err.println("returned"));
////                memory.installIntercept(ret + 2, new AtomicFetchIntercept(cpu, () -> System.err.println("ret0")), false);
//                System.err.println("KEYV - test SHIFT/CTRL");
//            } else if (C && !V) {
////                final int retLO = cpu.peekByte(7);
////                final int retHI = cpu.peekByte(8);
////                final int ret = (retLO & 0xFF) | ((retHI & 0xFF) << 8);
//////                for (int i = 0; i < 10; i++) {
//////                    System.err.println("stack " + i + " = " + Util.formatHexByte(cpu.peekByte(i)));
//////                }
////                cpu.onReturnTo(ret, () -> System.err.println("KEYV - returned with key pressed = " + ((cpu.getX() & 0x80) != 0)));
////                System.err.println("KEYV - scan keyboard (OSBYTE &79): code = " + Util.formatHexByte(cpu.getX() & 0x7f) + " scan = " + ((cpu.getX() & 0x80) != 0) + " ret = " + Util.formatHexWord(ret));
////                memory.installIntercept(ret, new AtomicFetchIntercept(cpu, () -> System.err.println("ret0")), false);
//            } else if (!C && V) {
//                System.err.println("KEYV - key pressed interrupt");
//            } else {
//                System.err.println("KEYV - timer interrupt entry");
//            }
        }), false);
    }

    private State savedState;

    public void saveState() {
        cpu.setQuiescentCallback(() -> {
            try {
                savedState = createState();
                savedState.write(STATE_FILE);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void restoreState() {
        if (STATE_FILE.exists()) {
            cpu.setQuiescentCallback(() -> {
                try {
                    savedState = State.read(STATE_FILE);
                    restoreState(savedState);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        }
    }

    public void requestState(final Consumer<State> resultConsumer) {
        cpu.setQuiescentCallback(() -> {
            try {
                resultConsumer.accept(createState());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private State createState() throws Exception {
        final State state = new State();
        Util.populateState(state, videoULA);
        Util.populateState(state, systemVIA);
        Util.populateState(state, userVIA);
        Util.populateState(state, crtc6845);
        Util.populateState(state, cpu);
        Util.populateState(state, ram);
        return state;
    }

    private void restoreState(final State state) throws Exception {
        Util.applyState(state, videoULA);
        Util.applyState(state, systemVIA);
        Util.applyState(state, userVIA);
        Util.applyState(state, crtc6845);
        Util.applyState(state, cpu);
        Util.applyState(state, ram);
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
