package com.jbeeb.display;


public enum DisplayMode {

    MODE0(0x3000, 640, 256, 1, 80, 20480),
    MODE1(0x3000, 320, 256, 2, 80, 20480),
    MODE2(0x3000, 160, 256, 4, 80, 20480),
    MODE3(0x4000, 640, 200, 1, 80, 16000),
    MODE4(0x5800, 320, 256, 1, 40, 10240),
    MODE5(0x5800, 160, 256, 2, 40, 10240),
    MODE6(0x6000, 320, 200, 1, 40, 8000),
    MODE7(0x7C00, 640, 256, 1, 40, 1000);

    private final int memoryLocation;
    private final int width;
    private final int height;
    private final int bitsPerPixel;
    private final int physicalCharsPerLine;
    private final int size;

    private DisplayMode(
            int memoryLocation,
            int width,
            int height,
            int bitsPerPixel,
            int physicalCharsPerLine,
            int size
    ) {
        this.memoryLocation = memoryLocation;
        this.width = width;
        this.height = height;
        this.bitsPerPixel = bitsPerPixel;
        this.physicalCharsPerLine = physicalCharsPerLine;
        this.size = size;
    }

    public int getMemoryLocation() {
        return memoryLocation;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getBitsPerPixel() {
        return bitsPerPixel;
    }

    public int getPhysicalCharsPerLine() {
        return physicalCharsPerLine;
    }

    public int getSize() {
        return size;
    }

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

    public int getLogicalColour(final int v, final int position) {
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
}
