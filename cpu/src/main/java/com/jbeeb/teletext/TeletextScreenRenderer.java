package com.jbeeb.teletext;

import com.jbeeb.device.CRTC6845;
import com.jbeeb.device.SystemVIA;
import com.jbeeb.device.VideoULA;
import com.jbeeb.screen.AbstractScreenRenderer;
import com.jbeeb.screen.DisplayMode;
import com.jbeeb.memory.Memory;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import static com.jbeeb.teletext.TeletextConstants.TELETEXT_CHAR_HEIGHT;
import static com.jbeeb.teletext.TeletextConstants.TELETEXT_CHAR_WIDTH;

public final class TeletextScreenRenderer extends AbstractScreenRenderer {

    private final TeletextRenderer renderer = new TeletextRenderer();

    public TeletextScreenRenderer(Memory memory, SystemVIA systemVIA, CRTC6845 crtc6845, VideoULA videoULA) {
        super(memory, systemVIA, crtc6845, videoULA);
    }

    @Override
    public boolean isClockBased() {
        return false;
    }

    @Override
    public void tick(DisplayMode mode, BufferedImage image) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isImageReady() {
        return true;
    }

    @Override
    public void vsync() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void refreshWholeImage(DisplayMode mode, BufferedImage img) {
        final Graphics2D g = img.createGraphics();

        final int leftMargin = (img.getWidth() - (TELETEXT_CHAR_WIDTH * 40)) / 2;

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, img.getWidth(), img.getHeight());

        final int unadjustedStartAddress = crtc6845.getScreenStartAddress();
        int address = adjustMode7Address(unadjustedStartAddress);
        final int cursorAddress = adjustMode7Address(crtc6845.getCursorAddress());

        Rectangle cursorRect = null;

        for (int y = 0; y < (TELETEXT_CHAR_HEIGHT * 25); y += TELETEXT_CHAR_HEIGHT) {

            renderer.resetToDefaults();

            for (int x = leftMargin; x < (TELETEXT_CHAR_WIDTH * 40 + leftMargin); x += TELETEXT_CHAR_WIDTH) {
                if (crtc6845.isCursorOn() && address == cursorAddress) {
                    cursorRect = new Rectangle(x, y, TELETEXT_CHAR_WIDTH, TELETEXT_CHAR_HEIGHT);
                }
                renderer.paintCell(g, memory.readByte(address), x, y, TELETEXT_CHAR_WIDTH, TELETEXT_CHAR_HEIGHT);
                address++;
                if (address >= 0x8000) {
                    address -= 1024;
                }
            }
        }

        paintCursor(g, cursorRect, TELETEXT_CHAR_HEIGHT / 8);
    }

    private static int adjustMode7Address(final int unadjustedAddress) {
        final int addrH = ((((unadjustedAddress >>> 8) & 0xFF) ^ 0x20) + 0x74);
        return (unadjustedAddress & 0xFF) | ((addrH & 0xFF) << 8);
    }
}
