package com.jbeeb.screen;

import com.jbeeb.device.Crtc6845;
import com.jbeeb.device.SystemVIA;
import com.jbeeb.device.VideoULA;
import com.jbeeb.memory.Memory;

import java.awt.*;

public abstract class AbstractScreenRenderer implements ScreenRenderer {

    protected final Memory memory;
    protected final SystemVIA systemVIA;
    protected final Crtc6845 crtc6845;
    protected final VideoULA videoULA;

    public AbstractScreenRenderer(
            final Memory memory,
            final SystemVIA systemVIA,
            final Crtc6845 crtc6845,
            final VideoULA videoULA
    ) {
        this.memory = memory;
        this.systemVIA = systemVIA;
        this.crtc6845 = crtc6845;
        this.videoULA = videoULA;
    }

    protected final void paintCursor(final Graphics2D g, final Rectangle charRect, final int pixelHeight) {
        if (charRect != null) {
            g.setColor(Color.WHITE);
            g.fillRect(charRect.x, charRect.y + charRect.height - pixelHeight, charRect.width, pixelHeight);
        }
    }
}
