package com.jbeeb.display;

import java.awt.image.BufferedImage;

public interface DisplayRenderer {
    void refreshImage(DisplayMode mode, BufferedImage image);
}
