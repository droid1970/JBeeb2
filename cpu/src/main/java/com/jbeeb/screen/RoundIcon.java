package com.jbeeb.screen;

import javax.swing.Icon;
import java.awt.*;

class RoundIcon implements Icon {

    private final int width;
    private final int height;
    private final int yoffset;

    private Color colour;

    RoundIcon(final int width, final int height, final int yoffset) {
        this.width = width;
        this.height = height;
        this.yoffset = yoffset;
    }

    void setColour(final Color colour) {
        this.colour = colour;
    }

    @Override
    public void paintIcon(Component c, Graphics g1, int x, int y) {
        final Graphics2D g = (Graphics2D) g1;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(colour);
        g.fillOval((getIconWidth() - 8) / 2, (getIconHeight() - 8) / 2 + yoffset, 8, 8);
    }

    @Override
    public int getIconWidth() {
        return width;
    }

    @Override
    public int getIconHeight() {
        return height;
    }
}