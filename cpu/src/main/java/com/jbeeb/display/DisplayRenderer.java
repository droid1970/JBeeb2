package com.jbeeb.display;

import java.awt.*;
import java.awt.image.BufferedImage;

public interface DisplayRenderer {
    void refreshImage(DisplayMode mode, BufferedImage image);

    default boolean isImageReady() {
        return true;
    }

    default Rectangle getCursorRect() {
        return null;
    }

    boolean isClockBased();
    void tick(final DisplayMode mode, final BufferedImage image);
    void vsync();
}
