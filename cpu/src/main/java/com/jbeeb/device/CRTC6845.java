package com.jbeeb.device;

import com.jbeeb.util.InterruptSource;
import com.jbeeb.util.ClockListener;
import com.jbeeb.util.SystemStatus;
import com.jbeeb.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CRTC6845 extends AbstractMemoryMappedDevice implements InterruptSource, ClockListener {

    private static final boolean TICK_WITH_EXECUTOR = false;

    private final ScheduledExecutorService executor;
    private final SystemVIA systemVIA;

    private static final int VERTICAL_SYNC_FREQUENCY_HZ = 50;
    private static final long VERTICAL_SYNC_CYCLE_COUNT = 2_000_000L / VERTICAL_SYNC_FREQUENCY_HZ;

    private static final long FAST_CURSOR_CYCLE_COUNT = VERTICAL_SYNC_CYCLE_COUNT * 8;
    private static final long SLOW_CURSOR_CYCLE_COUNT = VERTICAL_SYNC_CYCLE_COUNT * 16;

    private int v0;
    private int v1;

    private final int[] registers = new int[18];
    private AtomicInteger cursorAddress = new AtomicInteger();
    private final List<Runnable> vsyncListeners = new ArrayList<>();

    private long cycleCount = 0L;
    private boolean cursorOn;
    private boolean cursorBlink;
    private long cursorToggleCycles = FAST_CURSOR_CYCLE_COUNT;

    public CRTC6845(
            final SystemStatus systemStatus,
            final String name,
            final int startAddress,
            SystemVIA systemVIA
    ) {
        super(systemStatus, name, startAddress, 8);
        if (TICK_WITH_EXECUTOR) {
            this.executor = Executors.newScheduledThreadPool(1, r -> {
                final Thread t = new Thread(r, "crtc-6845-vsync");
                t.setDaemon(true);
                return t;
            });
            this.executor.scheduleAtFixedRate(this::verticalSync, 1000L / VERTICAL_SYNC_FREQUENCY_HZ, 1000L / VERTICAL_SYNC_FREQUENCY_HZ, TimeUnit.MILLISECONDS);
        } else {
            this.executor = null;
        }
        this.systemVIA = Objects.requireNonNull(systemVIA);
    }

    public boolean isCursorOn() {
        return !cursorBlink || cursorOn;
    }

    @Override
    public void tick() {
        if (!TICK_WITH_EXECUTOR) {
            if ((cycleCount % cursorToggleCycles) == 0) {
                cursorOn = !cursorOn;
            }
            final long syncCycle = (cycleCount % VERTICAL_SYNC_CYCLE_COUNT);
            if (syncCycle == 0) {
                systemVIA.setCA1(true);
                verticalSync();
            } else {
                if (syncCycle == 500) {
                    systemVIA.setCA1(false);
                }
            }
            cycleCount++;
        }
    }

    public void addVSyncListener(final Runnable l) {
        vsyncListeners.add(Objects.requireNonNull(l));
    }

    private void verticalSync() {
        vsyncListeners.forEach(Runnable::run);
    }

    @Override
    public boolean isIRQ() {
        return false;
    }

    @Override
    public boolean isNMI() {
        return false;
    }

    private boolean isReadOnly(final int index) {
        return (index >= 16 && index <= 17);
    }

    private boolean isWriteOnly(final int index) {
        return (index >= 0 && index <= 13);
    }

    public int getHorizontalTotalChars() {
        return registers[0];
    }

    public int getHorizontalDisplayedChars() {
        return registers[1];
    }

    public int getHorizontalSyncPosition() {
        return registers[2];
    }

    public int getScanlinesPerCharacter() {
        return registers[9];
    }

    public int getVerticalDisplayedChars() {
        return registers[6];
    }

    public int getScreenStartAddress() {
        return ((registers[12] & 0xFF) << 8) | (registers[13] & 0xFF);
    }

    public int getCursorAddress() {
        return cursorAddress.get();
    }

    private int computeCursorAddress() {
        return ((registers[14] & 0xFF) << 8) | (registers[15] & 0xFF);
    }

    private int readInternalRegister(final int index) {
        if (isWriteOnly(index)) {
            return 0;
        } else {
            return registers[v0];
        }
    }

    @Override
    public int readRegister(int index) {
        index &= 1;
        if (index == 0) {
            return v0;
        } else {
            return readInternalRegister(v0);
        }
    }

    @Override
    public void writeRegister(int index, int value) {
        index &= 1;
        if (index == 0) {
            v0 = value;
        } else {
            if (isReadOnly(v0)) {
                // Do nothing
            } else {
                registers[v0] = value & 0xFF;
                if (v0 == 15) {
                    cursorAddress.set(computeCursorAddress());
                    //Util.log(getName() + " - write " + v0 + " = " + value + " / " + Util.formatHexByte(value), 0);
                }
                if (v0 == 10) {
                    cursorBlink = (value & 0x40) != 0;
                    cursorToggleCycles = (value & 0x20) != 0 ? SLOW_CURSOR_CYCLE_COUNT : FAST_CURSOR_CYCLE_COUNT;
                }
            }
        }
    }
}
