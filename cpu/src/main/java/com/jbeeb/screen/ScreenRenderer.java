package com.jbeeb.screen;

import java.awt.image.BufferedImage;

public interface ScreenRenderer {

    void refreshWholeImage(DisplayMode mode, BufferedImage image);

    boolean isImageReady();

    boolean isClockBased();
    void tick(final DisplayMode mode, final BufferedImage image);
    void vsync();
}
