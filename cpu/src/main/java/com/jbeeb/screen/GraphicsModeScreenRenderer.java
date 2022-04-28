package com.jbeeb.screen;

import com.jbeeb.device.CRTC6845;
import com.jbeeb.device.SystemVIA;
import com.jbeeb.device.VideoULA;
import com.jbeeb.memory.Memory;

import java.awt.*;
import java.awt.image.BufferedImage;

public class GraphicsModeScreenRenderer extends AbstractScreenRenderer {

    private final Screen screen;

    private long cyclesSinceVSync = 0L;
    private int scanLine;
    private Rectangle cursorRect;

    public GraphicsModeScreenRenderer(Screen screen, Memory memory, SystemVIA systemVIA, CRTC6845 crtc6845, VideoULA videoULA) {
        super(memory, systemVIA, crtc6845, videoULA);
        this.screen = screen;
    }

    @Override
    public boolean isClockBased() {
        return true;
    }

    @Override
    public void tick(DisplayMode mode, BufferedImage image) {
        try {
            paintNextScanline(mode, image);
        } catch (Exception ex) {
            // Deliberately ignored
        }
    }

    @Override
    public void vsync() {
        cyclesSinceVSync = 0L;
        scanLine = 0;
        cursorRect = null;
    }

    @Override
    public boolean isImageReady() {
        return scanLine > 255;
    }

    public void paintNextScanline(final DisplayMode mode, final BufferedImage img) {
        if (scanLine > 255) {
            return;
        }

        if (scanLine == 0) {
            cursorRect = null;
        }

        final int startAddressUnadjusted  = crtc6845.getScreenStartAddress();
        final int startAddress = startAddressUnadjusted * 8;
        final int cursorAddressUnadjusted = crtc6845.getCursorAddress();
        final int cursorAddress = cursorAddressUnadjusted * 8;
        if (cursorAddress < startAddress) {
            int x = 1;
        }
        final boolean cursorOn = crtc6845.isCursorOn();

        final int baseAddress = mode.getMemoryLocation();
        final int charsPerLine = mode.getPhysicalCharsPerLine();
        final int pw = img.getWidth() / mode.getWidth();
        final int ph = img.getHeight() / mode.getHeight();
        final int pixelsPerByte = 8 / mode.getBitsPerPixel();
        final int byteWidth = img.getWidth() / mode.getPhysicalCharsPerLine();
        final int scanLineAddress = startAddress + ((scanLine >>> 3) * charsPerLine * 8) + (scanLine & 0x7);
        for (int col = 0; col < charsPerLine; col++) {
            final int address = wrapAddress(baseAddress, scanLineAddress + (col << 3));
            final int v = memory.readByte(address);
            final int x = col * byteWidth;
            int px = x;
            for (int b = 0; b < pixelsPerByte; b++) {
                final int rgb = videoULA.getPhysicalColor(mode.getLogicalColour(v, b), mode.getBitsPerPixel()).getRGB() & 0xFFFFFF;
                fillRect(img, rgb, px, scanLine * ph, pw, ph);
                px += pw;
            }

            if (cursorOn && cursorRect == null && address == cursorAddress) {
                cursorRect = new Rectangle(x, (scanLine & 0xf8) * ph, byteWidth * mode.getBitsPerPixel(), ph * 8);
            }
        }
        scanLine++;
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
        if (isClockBased()) {
            throw new UnsupportedOperationException();
        }

        final Graphics2D g = img.createGraphics();

        final int startAddressUnadjusted  = crtc6845.getScreenStartAddress();
        final int cursorAddressUnadjusted = crtc6845.getCursorAddress();

        final int startAddress = startAddressUnadjusted * 8;
        final int cursorAddress = cursorAddressUnadjusted * 8;
        final int charsPerLine = mode.getPhysicalCharsPerLine();
        final int pw = img.getWidth() / mode.getWidth();
        final int ph = img.getHeight() / mode.getHeight();

        int address = startAddress;
        if (address > 0x8000) {
            return;
        }

        final int addressCount = mode.getSize();
        final int bytesPerCharacterLine = mode.getPhysicalCharsPerLine() * 8;

        final int byteWidth = img.getWidth() / mode.getPhysicalCharsPerLine();
        final int pixelsPerByte = 8 / mode.getBitsPerPixel();

        final boolean cursorOn = crtc6845.isCursorOn();

        Rectangle cursorRect = null;

        for (int i = 0; i < addressCount; i++) {

            final int v = memory.readByte(address);

            // Compute position on screen
            final int charRow = (i / bytesPerCharacterLine);
            final int charCol = (i >>> 3) % charsPerLine;
            final int charLine = (i & 0x7);
            final int x = charCol * byteWidth;

            final int y = charRow * 8 * ph + (charLine * ph);

            if (cursorOn && address == cursorAddress) {
                final int cy = charRow * 8 * ph + (7 * ph);
                cursorRect = new Rectangle(x, charRow * 8 * ph, byteWidth * mode.getBitsPerPixel(), ph * 8);
            }

            int px = x;
            for (int b = 0; b < pixelsPerByte; b++) {
                final int rgb = videoULA.getPhysicalColor(mode.getLogicalColour(v, b), mode.getBitsPerPixel()).getRGB() & 0xFFFFFF;
                fillRect(img, rgb, px, y, pw, ph);
                px += pw;
            }

            address++;
            if (address == 0x8000) {
                address = mode.getMemoryLocation();
            }
        }

        paintCursor(g, cursorRect, ph);
    }

    private static void fillRect(final BufferedImage img, final int rgb, final int x, final int y, final int width, final int height) {
        for (int rx = x; rx < x + width; rx++) {
            for (int ry = y; ry < y + height; ry++) {
                img.setRGB(rx, ry, rgb);
            }
        }
    }
}
