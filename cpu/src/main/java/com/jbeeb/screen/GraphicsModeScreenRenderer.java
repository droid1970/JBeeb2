package com.jbeeb.screen;

import com.jbeeb.device.CRTC6845;
import com.jbeeb.device.SystemVIA;
import com.jbeeb.device.VideoULA;
import com.jbeeb.memory.Memory;

import java.awt.*;
import java.awt.image.BufferedImage;

public class GraphicsModeScreenRenderer extends AbstractScreenRenderer {

    private final Screen screen;

    private long ticksSinceSync = 0L;
    private int scanLine;
    private Rectangle cursorRect;

    private int startAddress;
    private int cursorAddress;
    private boolean cursorOn;
    private int baseAddress;
    private int charsPerLine;

    private int horizontalTotal;
    private int horizontalDisplayed;
    private int verticalTotal;
    private int verticalDisplayed;
    private int verticalAdjust;

    public GraphicsModeScreenRenderer(Screen screen, Memory memory, SystemVIA systemVIA, CRTC6845 crtc6845, VideoULA videoULA) {
        super(memory, systemVIA, crtc6845, videoULA);
        this.screen = screen;
    }

    @Override
    public void vsync(final DisplayMode mode) {

        startAddress = crtc6845.getScreenStartAddress() * 8;
        cursorAddress = wrapAddress(mode.getMemoryLocation(), crtc6845.getCursorAddress() * 8);
        cursorOn = crtc6845.isCursorOn();
        baseAddress = mode.getMemoryLocation();
        charsPerLine = mode.getPhysicalCharsPerLine();

        ticksSinceSync = 0L;
        charPos = 0;
        scanLine = 0;
        cursorRect = null;
        setTimings(
                crtc6845.getHorizontalTotalChars(),
                crtc6845.getHorizontalDisplayedChars(),
                crtc6845.getVerticalTotalChars(),
                crtc6845.getVerticalDisplayedChars(),
                crtc6845.getVerticalAdjust()
        );
    }

    @Override
    public boolean isClockBased() {
        return true;
    }

    @Override
    public void tick(DisplayMode mode, BufferedImage image) {
        try {
            paintNextCharacter(mode, image);
        } catch (Exception ex) {
            // Deliberately ignored
        }
        ticksSinceSync++;
    }

    private void setTimings(
            final int horizontalTotal,
            final int horizontalDisplayed,
            final int verticalTotal,
            final int verticalDisplayed,
            final int verticalAdjust
    ) {
        boolean changed = false;
        if (this.horizontalTotal != horizontalTotal) {
            this.horizontalTotal = horizontalTotal;
            changed = true;
        }
        if (this.horizontalDisplayed != horizontalDisplayed) {
            this.horizontalDisplayed = horizontalDisplayed;
            changed = true;
        }
        if (this.verticalTotal != verticalTotal) {
            this.verticalTotal = verticalTotal;
            changed = true;
        }
        if (this.verticalDisplayed != verticalDisplayed) {
            this.verticalDisplayed = verticalDisplayed;
            changed = true;
        }
        if (this.verticalAdjust != verticalAdjust) {
            this.verticalAdjust = verticalAdjust;
            changed = true;
        }
        if (changed) {
            System.err.println("hdisp = " + horizontalDisplayed + " vdisp = " + verticalDisplayed);
            System.err.println("htotal = " + horizontalTotal + " vtotal = " + verticalTotal + " vedj = " + verticalAdjust);
            System.err.println("total clocks = " + (horizontalTotal * (verticalTotal * 8) + verticalAdjust));
            System.err.println("fast clock = " + videoULA.isFastClockRate());
        }
    }

    @Override
    public boolean isImageReady() {
        return scanLine > 255;
    }

    private int charPos = 0;
    private void paintNextCharacter(final DisplayMode mode, final BufferedImage img) {
        if (scanLine > 255) {
            return;
        }
        if (scanLine == 0 && charPos == 0) {
            cursorRect = null;
        }

        final int pw = img.getWidth() / mode.getWidth();
        final int ph = img.getHeight() / mode.getHeight();
        final int pixelsPerByte = 8 / mode.getBitsPerPixel();
        final int byteWidth = img.getWidth() / mode.getPhysicalCharsPerLine();
        final int scanLineAddress = startAddress + ((scanLine >>> 3) * charsPerLine * 8) + (scanLine & 0x7);

        final int address = wrapAddress(baseAddress, scanLineAddress + (charPos << 3));
        final int v = memory.readByte(address);
        final int x = charPos * byteWidth;
        int px = x;
        for (int b = 0; b < pixelsPerByte; b++) {
            final int rgb = videoULA.getPhysicalColor(mode.getLogicalColour(v, b), mode.getBitsPerPixel()).getRGB() & 0xFFFFFF;
            fillRect(img, rgb, px, scanLine * ph, pw, ph);
            px += pw;
        }

        if (cursorOn && cursorRect == null && address == cursorAddress) {
            cursorRect = new Rectangle(x, (scanLine & 0xf8) * ph, byteWidth * mode.getBitsPerPixel(), ph * 8);
        }

        charPos++;
        if (charPos == horizontalDisplayed) {
            charPos = 0;
            scanLine++;
        }
        if (scanLine > 255) {
            if (cursorRect != null) {
                fillRect(img, Color.WHITE.getRGB(), cursorRect.x, cursorRect.y + cursorRect.height - ph , cursorRect.width, ph);
                cursorRect = null;
            }
            screen.imageReady();
        }
    }

    private static int wrapAddress(final int baseAddress, int address) {
        if (address >= 0x8000) {
            address -= (0x8000 - baseAddress);
        }
        return address;
    }

    @Override
    public void refreshWholeImage(final DisplayMode mode, final BufferedImage img) {
        throw new UnsupportedOperationException();
    }

    private static void fillRect(final BufferedImage img, final int rgb, final int x, final int y, final int width, final int height) {
        for (int rx = x; rx < x + width; rx++) {
            for (int ry = y; ry < y + height; ry++) {
                img.setRGB(rx, ry, rgb);
            }
        }
    }
}
