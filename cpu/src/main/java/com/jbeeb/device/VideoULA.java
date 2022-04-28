package com.jbeeb.device;

import com.jbeeb.util.InterruptSource;
import com.jbeeb.util.SystemStatus;

import java.awt.*;

public class VideoULA extends AbstractMemoryMappedDevice implements InterruptSource {

    private static final PhysicalColor[] PHYSICAL_COLORS = new PhysicalColor[]{
            PhysicalColor.solid(Color.BLACK),
            PhysicalColor.solid(Color.RED),
            PhysicalColor.solid(Color.GREEN),
            PhysicalColor.solid(Color.YELLOW),
            PhysicalColor.solid(Color.BLUE),
            PhysicalColor.solid(Color.MAGENTA),
            PhysicalColor.solid(Color.CYAN),
            PhysicalColor.solid(Color.WHITE),
            PhysicalColor.flashing(Color.BLACK, Color.WHITE),
            PhysicalColor.flashing(Color.RED, Color.CYAN),
            PhysicalColor.flashing(Color.GREEN, Color.MAGENTA),
            PhysicalColor.flashing(Color.YELLOW, Color.BLUE),
            PhysicalColor.flashing(Color.BLUE, Color.YELLOW),
            PhysicalColor.flashing(Color.MAGENTA, Color.GREEN),
            PhysicalColor.flashing(Color.CYAN, Color.RED),
            PhysicalColor.flashing(Color.WHITE, Color.BLACK)
    };

    private int masterCursorSize;
    private int cursorWidthBbytes;
    private int clockRate;
    private int charactersPerLine;
    private int teletext;
    private int flashIndex;

    private final int[] palette = {
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15
    };

    public VideoULA(final SystemStatus systemStatus, final String name, final int startAddress) {
        super(systemStatus, name, startAddress, 8);
    }

    @Override
    public boolean isIRQ() {
        return false;
    }

    @Override
    public boolean isNMI() {
        return false;
    }

    public int getCharactersPerLine() {
        switch (charactersPerLine) {
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

    public boolean isFastClockRate() {
        return clockRate > 0;
    }

    public boolean isTeletext() {
        return teletext > 0;
    }

    @Override
    public int readRegister(int index) {
        // Registers are write-only
        return 0;
    }

    @Override
    public void writeRegister(int index, int value) {
        index = index & 1;
        if (index == 0) {
            this.masterCursorSize = (value >>> 7) & 0x01;
            this.teletext = (value & 0x02) >>> 1;
            this.cursorWidthBbytes = (value >>> 5) & 0x03;
            this.clockRate = (value >>> 4) & 0x01;
            this.charactersPerLine = (value >>> 2) & 0x03;
            this.flashIndex = value & 0x01;
        } else if (index == 1) {
            final int logicalIndex = (value >>> 4) & 0x0F;
            final int actualColour = (value & 0x0F);
            palette[logicalIndex] = actualColour ^ 0x7;
        }
    }

    public Color getPhysicalColor(int logicalColorIndex, final int bitsPerPixel) {
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
        return PHYSICAL_COLORS[palette[paletteIndex]].getCurrentColor(flashIndex);
    }

    private static final class PhysicalColor {

        final Color color1;
        final Color color2;

        PhysicalColor(Color color1, Color color2) {
            this.color1 = color1;
            this.color2 = color2;
        }

        Color getCurrentColor(final int flashIndex) {
            return ((flashIndex & 1) == 0) ? color1 : color2;
        }

        static PhysicalColor solid(final Color c) {
            return new PhysicalColor(c, c);
        }

        static PhysicalColor flashing(final Color c1, final Color c2) {
            return new PhysicalColor(c1, c2);
        }
    }
}
