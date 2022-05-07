package com.jbeeb.teletext;

import com.jbeeb.clock.ClockSpeed;
import com.jbeeb.device.Crtc6845;
import com.jbeeb.device.SystemVIA;
import com.jbeeb.device.VideoULA;
import com.jbeeb.screen.AbstractScreenRenderer;
import com.jbeeb.memory.Memory;
import com.jbeeb.screen.SystemPalette;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;

import static com.jbeeb.teletext.TeletextConstants.TELETEXT_CHAR_HEIGHT;
import static com.jbeeb.teletext.TeletextConstants.TELETEXT_CHAR_WIDTH;

public final class TeletextScreenRenderer extends AbstractScreenRenderer {

    private final TeletextRenderer renderer = new TeletextRenderer();

    public TeletextScreenRenderer(Memory memory, SystemVIA systemVIA, Crtc6845 crtc6845, VideoULA videoULA) {
        super(memory, systemVIA, crtc6845, videoULA);
        final Timer flashTimer = new Timer("teletext-flasher", true);
        final TimerTask task = new TimerTask() {
            @Override
            public void run() {
                renderer.setTextShowing(!renderer.isTextShowing());
            }
        };
        flashTimer.scheduleAtFixedRate(task, 1000L, 1000L);
    }

    @Override
    public boolean isClockBased() {
        return false;
    }

    @Override
    public void tick(final BufferedImage image, final ClockSpeed clockSpeed, final long elapsedNanos) {
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
    public void refreshWholeImage(BufferedImage img) {
        final Graphics2D g = img.createGraphics();

        renderer.setBottom(true);

        final int leftMargin = (img.getWidth() - (TELETEXT_CHAR_WIDTH * 40)) / 2;

        g.setColor(SystemPalette.BLACK);
        g.fillRect(0, 0, img.getWidth(), img.getHeight());

        final int unadjustedStartAddress = crtc6845.getScreenStartAddress();
        int address = adjustMode7Address(unadjustedStartAddress);
        final int cursorAddress = adjustMode7Address(crtc6845.getCursorAddress());

        Rectangle cursorRect = null;

        for (int y = 0; y < (TELETEXT_CHAR_HEIGHT * 25); y += TELETEXT_CHAR_HEIGHT) {

            renderer.resetToDefaults();

            for (int x = leftMargin; x < (TELETEXT_CHAR_WIDTH * 40 + leftMargin); x += TELETEXT_CHAR_WIDTH) {
                if (crtc6845.isCursorEnabled() && crtc6845.isCursorOn() && videoULA.isCursorEnabled() && address == cursorAddress) {
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
