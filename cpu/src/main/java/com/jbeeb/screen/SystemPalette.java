package com.jbeeb.screen;

import java.awt.Color;
import java.util.Arrays;

public final class SystemPalette {

    private final Color[] colours;

    public SystemPalette(final Color... colours) {
        if (colours.length != 8) {
            throw new IllegalArgumentException(colours.length + ": there must be 8 colours here");
        }
        this.colours = Arrays.copyOf(colours, colours.length);
    }

    private static final int LO = 16;
    private static final int HI = 250;

    public static final Color BLACK = new Color(LO, LO, LO);
    public static final Color RED = new Color(HI, LO, LO);
    public static final Color GREEN = new Color(LO, HI, LO);
    public static final Color YELLOW = new Color(HI, HI, LO);
    public static final Color BLUE = new Color(LO, LO, HI);
    public static final Color MAGENTA = new Color(HI, LO, HI);
    public static final Color CYAN = new Color(LO, HI, HI);
    public static final Color WHITE = new Color(HI, HI, HI);

    public static final SystemPalette DEFAULT = new SystemPalette(
            BLACK, RED, GREEN,YELLOW, BLUE, MAGENTA, CYAN, WHITE
    );

    public Color getColour(final int colourIndex) {
        return colours[colourIndex];
    }
}
