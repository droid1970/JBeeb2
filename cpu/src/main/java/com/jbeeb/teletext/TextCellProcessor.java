package com.jbeeb.teletext;

import java.awt.*;
import java.awt.image.BufferedImage;

class TextCellProcessor implements CellProcessor {

    private final BufferedImage[] images;
    private final BufferedImage[] topImages;
    private final BufferedImage[] bottomImages;

    TextCellProcessor(final AlphaDefinition alphaDefinition) {
        this.images = new BufferedImage[TeletextConstants.getColourCount()];
        this.topImages = new BufferedImage[TeletextConstants.getColourCount()];
        this.bottomImages = new BufferedImage[TeletextConstants.getColourCount()];
        for (int i = 0; i < TeletextConstants.getColourCount(); i++) {
            images[i] = TeletextAlphaDefinition.createCharacterImage(alphaDefinition, TeletextConstants.getColour(i), false);
            final BufferedImage doubleHeightImage = TeletextAlphaDefinition.createCharacterImage(alphaDefinition, TeletextConstants.getColour(i), true);
            final int dh = doubleHeightImage.getHeight();
            topImages[i] = doubleHeightImage.getSubimage(0, 0, doubleHeightImage.getWidth(), dh / 2);
            bottomImages[i] = doubleHeightImage.getSubimage(0, dh / 2, doubleHeightImage.getWidth(), dh / 2);
        }
    }

    @Override
    public void process(TeletextRenderer renderer, Graphics2D g, int x, int y, int width, int height) {
        if (renderer.isTextSteady() || renderer.isTextShowing()) {
            if (renderer.isDoubleHeight()) {
                g.drawImage((renderer.isBottom() ? bottomImages : topImages)[renderer.getForegroundIndex()], x, y, null);
            } else {
                g.drawImage(images[renderer.getForegroundIndex()], x, y, null);
            }
        }
    }
}
