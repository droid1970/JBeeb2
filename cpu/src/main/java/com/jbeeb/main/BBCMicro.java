package com.jbeeb.main;

import com.jbeeb.assembler.Disassembler;
import com.jbeeb.clock.Clock;
import com.jbeeb.clock.ClockSpeed;
import com.jbeeb.cpu.Cpu;
import com.jbeeb.cpu.Flag;
import com.jbeeb.cpu.InstructionSet;
import com.jbeeb.device.*;
import com.jbeeb.disk.FloppyDiskController;
import com.jbeeb.localfs.FilingSystem;
import com.jbeeb.localfs.LocalFilingSystem;
import com.jbeeb.memory.*;
import com.jbeeb.screen.Screen;
import com.jbeeb.sound.SoundChip;
import com.jbeeb.util.*;

import java.io.File;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public final class BBCMicro implements InterruptSource {

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

    private static final boolean INSTALL_DFS = false;

    private static final File STATE_FILE = new File(System.getProperty("user.home"), "state.bbc");
    private static final String BASIC_ROM_RESOURCE_NAME = "/roms/BASIC2.rom";
    private static final String DFS_ROM_RESOURCE_NAME = "/roms/DFS-1.2.rom";
    private static final String OS_ROM_RESOURCE_NAME = "/roms/OS-1.2.rom";

    private static final int SHEILA = 0xFE00;

    private InterruptSource[] interruptSources = new InterruptSource[0];
    private final SystemStatus systemStatus;

    private final VideoULA videoULA;
    private final SystemVIA systemVIA;
    private final SoundChip soundChip;
    private final UserVIA userVIA;
    private final Crtc6845 crtc6845;
    private final ADC adc;
    private final FloppyDiskController fdc;
    private final PagedRomSelect pagedRomSelect;
    private final RandomAccessMemory ram;

    private final Cpu cpu;

    private final Clock clock;

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

        this.systemStatus = new SystemStatus();

        this.videoULA = new VideoULA(
                systemStatus,
                "Video ULA",
                SHEILA + 0x20
        );

        this.soundChip = SystemVIA.createSoundChip();

        this.systemVIA = new SystemVIA(
                systemStatus,
                soundChip,
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
        this.adc = new ADC(
                systemStatus,
                "ADC",
                SHEILA + 0xC0,
                scheduler,
                systemVIA
        );

        this.fdc = (INSTALL_DFS) ? new FloppyDiskController(systemStatus, scheduler, "FDC8271", SHEILA + 0x80) : null;

        this.pagedRomSelect = new PagedRomSelect(systemStatus, "Paged ROM", SHEILA + 0x30, 1);

        final List<MemoryMappedDevice> devices = new ArrayList<>();
        devices.add(videoULA);
        devices.add(systemVIA);
        devices.add(crtc6845);
        devices.add(userVIA);
        devices.add(adc);
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
        final FilingSystem filingSystemROM = new LocalFilingSystem("Local DFS", "(C) Ian T 2022");
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
        crtc6845.addNewFrameListener(screen::newFrame);

        this.cpu = new Cpu(systemStatus, scheduler, memory);
        filingSystemROM.initialise(cpu, memory);

        cpu.setVerboseCondition(() -> false);
        if (fdc != null) {
            this.fdc.setCpu(cpu);
        }

        this.clock = new Clock(
                systemStatus,
                ClockSpeed.CR200,
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
        if (false) scheduler.newTask(() -> {
            try {
                final Disassembler dis = new Disassembler(new InstructionSet(), memory);
                dis.setPC(0x1C28);
                for (int i = 0; i < 100; i++) {
                    System.out.println(dis.disassemble());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).schedule(50_000_000);

        final int KEYV = 0xEF02;
        final int INSV = 0xE4B3;

        if (false) memory.installIntercept(KEYV, new AtomicFetchIntercept(cpu, () -> {
            final boolean C = cpu.isFlagSet(Flag.CARRY);
            final boolean V = cpu.isFlagSet(Flag.OVERFLOW);
            if (!C && !V) {
                //System.err.println("KEYV - test SHIFT/CTRL");
            } else if (C && !V) {
                final int retLO = cpu.peekByte(7);
                final int retHI = cpu.peekByte(8);
                final int ret = (retLO & 0xFF) | ((retHI & 0xFF) << 8);
                //cpu.onReturnTo(ret, () -> System.err.println("KEYV - returned to " + Util.formatHexWord(ret + 1) + " with key pressed = " + ((cpu.getX() & 0x80) != 0)));
                //System.err.println("KEYV - scan keyboard (OSBYTE &79): code = " + Util.formatHexByte(cpu.getX() & 0x7f) + " key = " + BBCKey.forInternalCode(cpu.getX() & 0x7F) + " scan = " + ((cpu.getX() & 0x80) != 0) + " ret = " + Util.formatHexWord(ret));
            } else if (!C && V) {
                //System.err.println("KEYV - key pressed interrupt");
            } else {
                //System.err.println("KEYV - timer interrupt entry");
            }
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

    private State createState() throws Exception {
        final State state = new State();
        Util.populateState(state, videoULA);
        Util.populateState(state, systemVIA);
        Util.populateState(state, soundChip);
        Util.populateState(state, userVIA);
        Util.populateState(state, crtc6845);
        Util.populateState(state, cpu);
        Util.populateState(state, ram);
        return state;
    }

    private void restoreState(final State state) throws Exception {
        Util.applyState(state, videoULA);
        Util.applyState(state, systemVIA);
        Util.applyState(state, soundChip);
        Util.applyState(state, userVIA);
        Util.applyState(state, crtc6845);
        Util.applyState(state, cpu);
        Util.applyState(state, ram);
    }

    public void run(final BooleanSupplier haltCondition) {
        this.clock.run(haltCondition);
    }

    public void addInterruptSource(final InterruptSource source) {
        interruptSources = Arrays.copyOf(interruptSources, interruptSources.length + 1);
        interruptSources[interruptSources.length - 1] = source;
    }

    @Override
    public String getName() {
        return "BBC Micro Model B";
    }

    public Clock getClock() {
        return clock;
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
