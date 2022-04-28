package com.jbeeb.teletext;

import java.awt.*;
import java.awt.image.BufferedImage;

class TextCellProcessor implements CellProcessor {

    private final BufferedImage[] images;

    TextCellProcessor(final PathSpec pathSpec) {
        this.images = new BufferedImage[TeletextConstants.getColourCount()];
        for (int i = 0; i < TeletextConstants.getColourCount(); i++) {
            images[i] = TeletextCharacterDefinitions.createCharacterImage(pathSpec, TeletextConstants.getColour(i));
        }
    }

    @Override
    public void process(TeletextRenderer renderer, Graphics2D g, int x, int y, int width, int height) {
        g.drawImage(images[renderer.getForegroundIndex()], x, y, null);
    }
}
