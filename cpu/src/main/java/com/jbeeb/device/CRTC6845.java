package com.jbeeb.device;

import com.jbeeb.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

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

    @StateKey(key = "cursorAddress")
    private int cursorAddress;

    @StateKey(key = "cycleCount")
    private long cycleCount = 0L;

    @StateKey(key = "cursorOn")
    private boolean cursorOn;

    @StateKey(key = "cursorBlink")
    private boolean cursorBlink;

    @StateKey(key = "cursorToggleCycles")
    private long cursorToggleCycles = SLOW_CURSOR_CYCLE_COUNT;

    private final CursorDetails cursor = new CursorDetails();

    public CRTC6845(
            final SystemStatus systemStatus,
            final String name,
            final int startAddress,
            SystemVIA systemVIA
    ) {
        super(systemStatus, name, startAddress, 8);
        this.systemVIA = Objects.requireNonNull(systemVIA);
    }

    private static final class CursorDetails {

        int blankingDelay;
        boolean blinkEnabled;
        boolean fastBlink;
        int cursorStart;
        int cursorEnd;

        void setBlankingDelay(int n) {
            if (blankingDelay != n) {
                this.blankingDelay = n;
            }
        }

        void setBlinkEnabled(boolean b) {
            if (blinkEnabled != b) {
                blinkEnabled = b;
            }
        }

        void setFastBlink(boolean b) {
            if (fastBlink != b) {
                fastBlink = b;
            }
        }

        void setCursorStart(int n) {
            if (cursorStart != n) {
                cursorStart = n;
            }
        }

        void setCursorEnd(int n) {
            if (cursorEnd != n) {
                cursorEnd = n;
            }
        }

        boolean isCursorEnabled() {
            return cursorStart > 0 && (cursorStart < cursorEnd);
        }

        public String toString() {
            return "blankingDelay = " + blankingDelay + " blinkEnabled = " + blinkEnabled + " fastBlink = " + fastBlink + " start = " + cursorStart + " end = " + cursorEnd;
        }
    }

    public boolean isCursorEnabled() {
        return cursor.isCursorEnabled();
    }

    public boolean isCursorOn() {
        return !cursorBlink || cursorOn;
    }

    @Override
    public void tick() {
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

    public int getVerticalTotalChars() {
        return registers[4] + 1;
    }

    public int getVerticalAdjust() {
        return registers[5];
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
        return cursorAddress;
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
                if (v0 == 8) {
                    cursor.setBlankingDelay((value >>> 6) & 0x3);
                }
                if (v0 == 10) {
                    cursor.setBlinkEnabled((value & 0x40) != 0);
                    cursor.setFastBlink((value & 0x20) == 0);
                    cursor.setCursorStart(value & 0x1F);
                }
                if (v0 == 11) {
                    cursor.setCursorEnd(value & 0x1F);
                }
                if (v0 == 15) {
                    cursorAddress = computeCursorAddress();
                }
                if (v0 == 10) {
                    cursorBlink = (value & 0x40) != 0;
                    cursorToggleCycles = (value & 0x20) != 0 ? SLOW_CURSOR_CYCLE_COUNT : FAST_CURSOR_CYCLE_COUNT;
//                    System.err.println("6845: register " + v0 + " = " + Util.formatHexByte(value) + " blink = " + cursorBlink);
//                    System.err.println("76543210");
//                    System.err.println(Util.pad0(Integer.toBinaryString(value), 8));
                }

                //System.err.println("6845: register " + v0 + " = " + Util.formatHexByte(value));
            }
        }
    }
}
