package com.jbeeb.device;

import com.jbeeb.clock.ClockListener;
import com.jbeeb.clock.ClockSpeed;
import com.jbeeb.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@StateKey(key = "crtc6845")
public class Crtc6845 extends AbstractMemoryMappedDevice implements InterruptSource, ClockListener {

    private static final int CLOCK_RATE = ClockSpeed.TWO_MHZ;

    private static final int VERTICAL_SYNC_FREQUENCY_HZ = ClockSpeed.FIFTY_HZ;
    private static final int VERTICAL_SYNC_2MHZ_CYCLES = CLOCK_RATE / VERTICAL_SYNC_FREQUENCY_HZ;

    private static final int FAST_CURSOR_VSYNCS = 8;
    private static final int SLOW_CURSOR_VSYNCS = 16;

    private final SystemVIA systemVIA;
    private final List<Runnable> newFrameListeners = new ArrayList<>();

    @StateKey(key = "v0")
    private int v0;

    @StateKey(key = "registers")
    private final int[] registers = new int[18];

    private boolean cursorOn;
    private long inputCycleCount = 0L;
    private long myCycleCount = 0L;
    private long lastEndOfFrame = 0L;
    private long lastCursorBlink = 0L;

    private long firstFrameTime = -1L;
    private int frameCount;
    private boolean firedNewFrame = false;
    private boolean firedSyncOn = false;
    private boolean firedSyncOff = false;

    public Crtc6845(
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
    public void tick(final ClockSpeed clockSpeed, final long elapsedNanos) {
        final int cyclesPerRow = VERTICAL_SYNC_2MHZ_CYCLES / getVerticalTotalChars();
        final int cyclesPerScanline = VERTICAL_SYNC_2MHZ_CYCLES / (getVerticalTotalChars() * 8);
        final int syncPulseOnCycles = getVerticalSyncPosition() * cyclesPerRow;
        final int syncPulseOffCycles = syncPulseOnCycles + getVerticalSyncPulseWidth() * cyclesPerScanline;
        final long cyclesSinceLastNewFrame = myCycleCount - lastEndOfFrame;

        if (!firedNewFrame) {
            // New frame (start rendering)
            newFrame();
            firedNewFrame = true;
        }

        if (!firedSyncOn && cyclesSinceLastNewFrame >= syncPulseOnCycles) {
            // Fire vsync interrupt
            systemVIA.setCA1(true);
            firedSyncOn = true;
        }

        if (!firedSyncOff && cyclesSinceLastNewFrame >= syncPulseOffCycles) {
            // Stop vsync interrupt
            systemVIA.setCA1(false);
            firedSyncOff = true;
        }

        final long cursorToggleCycles = VERTICAL_SYNC_2MHZ_CYCLES * ((isCursorFastBlink()) ? FAST_CURSOR_VSYNCS : SLOW_CURSOR_VSYNCS);
        final long cyclesSinceLastCursorBlink = myCycleCount - lastCursorBlink;
        if (cyclesSinceLastCursorBlink >= cursorToggleCycles) {
            cursorOn = !cursorOn;
            lastCursorBlink = myCycleCount;
        }

        if ((cyclesSinceLastNewFrame >= VERTICAL_SYNC_2MHZ_CYCLES)) {
            lastEndOfFrame = myCycleCount;
            firedNewFrame = false;
            firedSyncOn = false;
            firedSyncOff = false;
        }

        myCycleCount += clockSpeed.computeElapsedCycles(CLOCK_RATE, inputCycleCount, myCycleCount, elapsedNanos);
        inputCycleCount++;
    }

    public void addNewFrameListener(final Runnable l) {
        newFrameListeners.add(Objects.requireNonNull(l));
    }

    private void newFrame() {
        if (firstFrameTime < 0L) {
            firstFrameTime = System.nanoTime();
        }
        frameCount++;
        if (frameCount == 40) { // Update status every couple of seconds
            final double secs = (System.nanoTime() - firstFrameTime) / 1_000_000_000.0;
            getSystemStatus().putDouble(SystemStatus.KEY_VSYNCS_PER_SECOND, frameCount / secs);
            firstFrameTime = System.nanoTime();
            frameCount = 0;
        }
        newFrameListeners.forEach(Runnable::run);
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

    public int getVerticalSyncPulseWidth() {
        return (registers[3] & 0xF0) >>> 4;
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
