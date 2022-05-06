package com.jbeeb.device;

import com.jbeeb.screen.SystemPalette;
import com.jbeeb.util.InterruptSource;
import com.jbeeb.util.StateKey;
import com.jbeeb.util.SystemStatus;

import java.awt.*;

@StateKey(key = "videoULA")
public class VideoULA extends AbstractMemoryMappedDevice implements InterruptSource {

    private static final PhysicalColor[] PHYSICAL_COLORS = new PhysicalColor[]{
            PhysicalColor.solid(0),
            PhysicalColor.solid(1),
            PhysicalColor.solid(2),
            PhysicalColor.solid(3),
            PhysicalColor.solid(4),
            PhysicalColor.solid(5),
            PhysicalColor.solid(6),
            PhysicalColor.solid(7),
            PhysicalColor.flashing(0, 7),
            PhysicalColor.flashing(1, 6),
            PhysicalColor.flashing(2, 5),
            PhysicalColor.flashing(3, 4),
            PhysicalColor.flashing(4, 3),
            PhysicalColor.flashing(5, 2),
            PhysicalColor.flashing(6, 1),
            PhysicalColor.flashing(7, 0)
    };

    private static final int[] BPP1_MASKS = {
            0b10000000,
            0b01000000,
            0b00100000,
            0b00010000,
            0b00001000,
            0b00000100,
            0b00000010,
            0b00000001
    };

    private static final int[] BPP2_MASKS = {
            0b10001000,
            0b01000100,
            0b00100010,
            0b00010001
    };

    private static final int[] BPP4_MASKS = {
            0b10101010,
            0b01010101
    };

    private SystemPalette systemPalette = SystemPalette.DEFAULT;

    @StateKey(key = "videoControlRegister")
    private int videoControlRegister;

    @StateKey(key = "palette")
    private final int[] palette = {
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15
    };

    public VideoULA(final SystemStatus systemStatus, final String name, final int startAddress) {
        super(systemStatus, name, startAddress, 8);
    }

    public boolean isCursorEnabled() {
        return getMasterCursorSize() > 0 || getCursorWidth() > 1;
    }

    @Override
    public boolean isIRQ() {
        return false;
    }

    @Override
    public boolean isNMI() {
        return false;
    }

    @Override
    public int readRegister(int index) {
        // Registers are write-only
        return 0;
    }
    public int getMasterCursorSize() {
        return (videoControlRegister >>> 7) & 0x01;
    }

    public int getCursorWidth() {
        final int cw = (videoControlRegister >>> 5) & 0x03;
        switch (cw) {
            default:
            case 0:
                return 1;
            case 2:
                return 2;
            case 3:
                return 4;
        }
    }

    public int getPixelsPerCharacter() {
        return 8 / getCursorWidth();
    }

    public boolean isFastClockRate() {
        return (videoControlRegister & 0x10) != 0;
    }

    public int getCharactersPerLine() {
        final int cpl = (videoControlRegister >>> 2) & 0x03;
        switch (cpl) {
            case 3:
                return 80;
            default:
            case 2:
                return 40;
            case 1:
                return 20;
            case 0:
                return 10;
        }
    }

    public int getSelectedFlashIndex() {
        return videoControlRegister & 0x01;
    }

    public boolean isTeletext() {
        return (videoControlRegister & 0x02) != 0;
    }

    @Override
    public void writeRegister(int index, int value) {
        index = index & 1;
        if (index == 0) {
            this.videoControlRegister = (value & 0xFF);
        } else if (index == 1) {
            final int logicalIndex = (value >>> 4) & 0x0F;
            final int actualColour = (value & 0x0F);
            palette[logicalIndex] = actualColour ^ 0x7;
        }
    }

    public Color getPhysicalColor(int v, int b) {
        final int bitsPerPixel = getCursorWidth();
        final int logicalColorIndex = getLogicalColour(v, b, bitsPerPixel);
        if (logicalColorIndex > 0) {
            int x = 1;
        }
        int paletteIndex = logicalColorIndex;
        switch (bitsPerPixel) {
            case 1:
                paletteIndex = logicalColorIndex * 8;
                break;
            case 2:
                switch (logicalColorIndex) {
                    case 0:
                        paletteIndex = 0;
                        break;
                    case 1:
                        paletteIndex = 2;
                        break;
                    case 2:
                        paletteIndex = 8;
                        break;
                    case 3:
                        paletteIndex = 10;
                        break;

                }
        }
        return PHYSICAL_COLORS[palette[paletteIndex]].getCurrentColour(systemPalette, getSelectedFlashIndex());
    }

    public static int getLogicalColour(final int v, final int position, final int bitsPerPixel) {
        switch (bitsPerPixel) {
            default:
            case 1:
                return ((v & BPP1_MASKS[position]) != 0) ? 1 : 0;
            case 2: {
                final int maskedAndShifted = (v & BPP2_MASKS[position]) >>> (3 - position);
                return (maskedAndShifted & 1) | (maskedAndShifted >>> 3);
            }
            case 4: {
                final int maskedAndShifted = (v & BPP4_MASKS[position]) >>> (1 - position);
                // 0 1 0 1 0 1 0 1
                return (maskedAndShifted & 1) |
                        ((maskedAndShifted >> 1) & 2) |
                        ((maskedAndShifted >> 2) & 4) |
                        ((maskedAndShifted >> 3) & 8);
            }
        }
    }

    private static final class PhysicalColor {

        final int index1;
        final int index2;

        PhysicalColor(final int index1, final int index2) {
            this.index1 = index1;
            this.index2 = index2;
        }

        Color getCurrentColour(final SystemPalette systemPalette, final int flashIndex) {
            return systemPalette.getColour(((flashIndex & 1) == 0) ? index1 : index2);
        }

        static PhysicalColor solid(final int index) {
            return new PhysicalColor(index, index);
        }

        static PhysicalColor flashing(final int index1, final int index2) {
            return new PhysicalColor(index1, index2);
        }
    }
}
