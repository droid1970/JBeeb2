package com.jbeeb.main;

import com.jbeeb.cpu.Cpu;
import com.jbeeb.device.*;
import com.jbeeb.disk.FloppyDiskController;
import com.jbeeb.memory.Memory;
import com.jbeeb.memory.PagedROM;
import com.jbeeb.memory.RandomAccessMemory;
import com.jbeeb.memory.ReadOnlyMemory;
import com.jbeeb.screen.Screen;
import com.jbeeb.util.*;

import java.io.File;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public final class BBCMicro implements InterruptSource {

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
        this.fdc = new FloppyDiskController(systemStatus, scheduler, "FDC8271", SHEILA + 0x80);

        this.pagedRomSelect = new PagedRomSelect(systemStatus, "Paged ROM", SHEILA + 0x30, 1);

        final List<MemoryMappedDevice> devices = new ArrayList<>();
        devices.add(videoULA);
        devices.add(systemVIA);
        devices.add(crtc6845);
        devices.add(userVIA);
        devices.add(fdc);
        devices.add(pagedRomSelect);
        devices.add(new SheilaMemoryMappedDevice(systemStatus));

        fdc.load(0, new File(System.getProperty("user.home"), "Arcadians.ssd"));
        fdc.load(1, new File(System.getProperty("user.home"), "Arcadians.ssd"));

        final ReadOnlyMemory basicRom = ReadOnlyMemory.fromResource(0x8000, BASIC_ROM_RESOURCE_NAME);
        final ReadOnlyMemory dfsRom = ReadOnlyMemory.fromResource(0x8000, DFS_ROM_RESOURCE_NAME);

        final Map<Integer, ReadOnlyMemory> roms = new HashMap<>();
        roms.put(15, basicRom);
        roms.put(12, dfsRom);

        final PagedROM pagedROM = new PagedROM(0x8000, 16384, pagedRomSelect, roms);

        for (int i = 0x8000; i < (0x8000 + 16384); i++) {
            int b1 = basicRom.readByte(i);
            int b2 = pagedROM.readByte(i);
            if (b1 != b2) {
                int x = 1;
            }
        }

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
        cpu.setVerboseSupplier(() -> false);
        this.fdc.setCpu(cpu);

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
        addInterruptSource(fdc);
        cpu.setInterruptSource(this);
    }

    private static final class TestTask implements Runnable {

        final Scheduler scheduler;
        final ScheduledTask task;

        int count;

        public TestTask(Scheduler scheduler) {
            this.scheduler = scheduler;
            this.task = scheduler.newTask(this);
            this.task.schedule(2_000_000);
        }

        @Override
        public void run() {
            System.err.println("Task: count = " + count);
            count++;
            if (count < 5) {
                task.schedule(2_000_000);
            }
        }
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
