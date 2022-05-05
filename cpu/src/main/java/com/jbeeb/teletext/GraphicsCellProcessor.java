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
        paintGraphics(g, bits, !renderer.isContiguousGraphics(), x, y, width, height);
    }

    private static void paintGraphics(final Graphics2D g, final int bits, final boolean gap, final int x, final int y, final int width, final int height) {
        final int pw = width / 2;
        final int gw = gap ? 2 : 0;
        final int gh = gap ? 2 : 0;

        if ((bits & 1) != 0) {
            g.fillRect(x, y, pw - gw, 6 - gh);
        }
        if ((bits & 2) != 0) {
            g.fillRect(x + pw, y, pw - gw, 6 - gh);
        }
        if ((bits & 4) != 0) {
            g.fillRect(x, y + 6, pw - gw, 8 - gh);
        }
        if ((bits & 8) != 0) {
            g.fillRect(x + pw, y + 6, pw - gw, 8 - gh);
        }
        if ((bits & 16) != 0) {
            g.fillRect(x, y + 14, pw - gw, 6 - gh);
        }
        if ((bits & 32) != 0) {
            g.fillRect(x + pw, y + 14, pw - gw, 6 - gh);
        }
    }
}
