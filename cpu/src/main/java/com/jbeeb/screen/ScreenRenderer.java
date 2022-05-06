package com.jbeeb.screen;

import java.awt.image.BufferedImage;

public interface ScreenRenderer {

    boolean isClockBased();
    void tick(final BufferedImage image);
    void vsync();

    void refreshWholeImage(BufferedImage image);
    boolean isImageReady();
}
