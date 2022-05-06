package com.jbeeb.teletext;

import com.jbeeb.screen.SystemPalette;

import java.awt.*;

final class TeletextConstants {

    private static final SystemPalette PALETTE = SystemPalette.DEFAULT;

    public static final int TELETEXT_CHAR_WIDTH = 14;
    public static final int TELETEXT_CHAR_HEIGHT = 20;
    public static final int TELETEXT_FLASH_PERIOD = 50;

    public static Color getColour(final int index) {
        return PALETTE.getColour(index);
    }

    public static int getColourCount() {
        return 8;
    }
}
