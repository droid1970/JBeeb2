package com.jbeeb.screen;

import com.jbeeb.clock.ClockSpeed;

import java.awt.image.BufferedImage;

public interface ScreenRenderer {

    boolean isClockBased();
    void tick(BufferedImage image, ClockSpeed clockSpeed, long elapsedNanos);
    void vsync();

    void refreshWholeImage(BufferedImage image);
    boolean isImageReady();
}
