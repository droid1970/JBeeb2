package com.jbeeb.teletext;

import java.awt.*;

final class TeletextConstants {

    private static final Color[] COLORS = {
            Color.BLACK,
            Color.RED,
            Color.GREEN,
            Color.YELLOW,
            Color.BLUE,
            Color.MAGENTA,
            Color.CYAN,
            Color.WHITE
    };

    public static final int TELETEXT_CHAR_WIDTH = 14;
    public static final int TELETEXT_CHAR_HEIGHT = 20;
    public static final int TELETEXT_FLASH_PERIOD = 50;

    public static Color getColour(final int index) {
        return COLORS[index];
    }

    public static int getColourCount() {
        return COLORS.length;
    }
}
