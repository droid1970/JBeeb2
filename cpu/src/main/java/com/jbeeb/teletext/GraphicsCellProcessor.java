package com.jbeeb.teletext;

import java.awt.*;

final class GraphicsCellProcessor implements CellProcessor {

    private final int bits;

    GraphicsCellProcessor(final int bits) {
        this.bits = bits;
    }

    @Override
    public void process(TeletextRenderer renderer, Graphics2D g, int x, int y, int width, int height) {
        g.setColor(renderer.getGraphicsColour());
        paintGraphics(g, bits, false, x, y, width, height);
    }

    private static void paintGraphics(final Graphics2D g, final int bits, final boolean gap, final int x, final int y, final int width, final int height) {
        final int pw = width / 2;
        final int ph = (int) Math.ceil(height / 3.0);
        final int lastPh = (height - (ph * 2));

        if ((bits & 1) != 0) {
            g.fillRect(x, y, pw, ph);
        }
        if ((bits & 2) != 0) {
            g.fillRect(x + pw, y, pw, ph);
        }
        if ((bits & 4) != 0) {
            g.fillRect(x, y + ph, pw, ph);
        }
        if ((bits & 8) != 0) {
            g.fillRect(x + pw, y + ph, pw, ph);
        }
        if ((bits & 16) != 0) {
            g.fillRect(x, y + ph + ph, pw, lastPh);
        }
        if ((bits & 32) != 0) {
            g.fillRect(x + pw, y + ph + ph, pw, lastPh);
        }
    }
}
