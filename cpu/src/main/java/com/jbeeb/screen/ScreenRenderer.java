package com.jbeeb.screen;

import java.awt.image.BufferedImage;

public interface ScreenRenderer {

    void refreshWholeImage(BufferedImage image);

    boolean isImageReady();

    boolean isClockBased();
    void tick(final BufferedImage image);
    void vsync();
}
