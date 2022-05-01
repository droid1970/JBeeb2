package com.jbeeb.device;

import com.jbeeb.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@StateKey(key = "crtc6845")
public class CRTC6845 extends AbstractMemoryMappedDevice implements InterruptSource, ClockListener {

    private static final int VERTICAL_SYNC_FREQUENCY_HZ = 50;
    private static final long VERTICAL_SYNC_CYCLE_COUNT = 2_000_000L / VERTICAL_SYNC_FREQUENCY_HZ;

    private static final long FAST_CURSOR_CYCLE_COUNT = VERTICAL_SYNC_CYCLE_COUNT * 8;
    private static final long SLOW_CURSOR_CYCLE_COUNT = VERTICAL_SYNC_CYCLE_COUNT * 16;

    private final SystemVIA systemVIA;
    private final List<Runnable> vsyncListeners = new ArrayList<>();

    @StateKey(key = "v0")
    private int v0;

    @StateKey(key = "registers")
    private final int[] registers = new int[18];

    private long cycleCount;
    private boolean cursorOn;

    public CRTC6845(
            final SystemStatus systemStatus,
            final String name,
            final int startAddress,
            SystemVIA systemVIA
    ) {
        super(systemStatus, name, startAddress, 8);
        this.systemVIA = Objects.requireNonNull(systemVIA);
    }

    public boolean isCursorEnabled() {
        return getCursorStartLine() > 0  && (getCursorStartLine() < getCursorEndLine());
    }

    public boolean isCursorOn() {
        return !isCursorBlinkEnabled() || cursorOn;
    }

    @Override
    public void tick() {
        final long cursorToggleCycles = (isCursorFastBlink()) ? FAST_CURSOR_CYCLE_COUNT : SLOW_CURSOR_CYCLE_COUNT;
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
        return registers[0] + 1;
    }

    public int getHorizontalDisplayedChars() {
        return registers[1];
    }

    public int getHorizontalSyncPosition() {
        return registers[2];
    }

    public int getVerticalTotalChars() {
        return (registers[4] & 0x7F) + 1;
    }

    public int getVerticalAdjust() {
        return registers[5] & 0x1F;
    }

    public int getVerticalDisplayedChars() {
        return registers[6] & 0x7F;
    }

    public int getVerticalSyncPosition() {
        return registers[7] & 0x7F;
    }

    public int getScanlinesPerCharacter() {
        return registers[9] + 1;
    }

    public int getScreenStartAddress() {
        return ((registers[12] & 0xFF) << 8) | (registers[13] & 0xFF);
    }

    //
    // Cursor stuff
    //
    public int getCursorAddress() {
        return computeCursorAddress();
    }

    public int getCursorBlankingDelay() {
        return (registers[8] >>> 6) & 0x3;
    }

    public boolean isCursorBlinkEnabled() {
        return (registers[10] & 0x40) != 0;
    }

    public boolean isCursorFastBlink() {
        return (registers[10] & 0x20) == 0;
    }

    public int getCursorStartLine() {
        return registers[10] & 0x1F;
    }

    public int getCursorEndLine() {
        return registers[11] & 0x1F;
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
            if (!isReadOnly(v0)) {
                registers[v0] = value & 0xFF;
            }
        }
    }
}
