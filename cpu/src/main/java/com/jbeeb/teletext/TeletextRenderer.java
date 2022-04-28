package com.jbeeb.teletext;

import java.awt.*;

final class TeletextRenderer {

    private final CellProcessorSet alphaProcessorSet = new CompoundCellProcessorSet(
            new ControlCodeProcessorSet(),
            new TextCellProcessorSet()
    );

    private final CellProcessorSet graphicsProcessorSet = new CompoundCellProcessorSet(
            new ControlCodeProcessorSet(),
            new GraphicsCellProcessorSet()
    );

    private CellProcessorSet cellProcessorSet;
    private int foregroundIndex;
    private Color background;
    private boolean graphicsEnabled;
    private Color graphicsColour;
    private boolean flashing;
    private boolean doubleHeight;
    private boolean conceal;
    private boolean contiguousGraphics;
    private boolean holdGraphics;

    public TeletextRenderer() {
        resetToDefaults();
    }

    public void resetToDefaults() {
        this.cellProcessorSet = alphaProcessorSet;
        this.foregroundIndex = 7;
        this.background = Color.BLACK;
        this.graphicsEnabled = false;
        this.graphicsColour = Color.WHITE;
        this.flashing = false;
        this.doubleHeight = false;
        this.conceal = false;
        this.contiguousGraphics = true;
        this.holdGraphics = false;
    }

    public int getForegroundIndex() {
        return foregroundIndex;
    }

    public Color getForeground() {
        return TeletextConstants.getColour(foregroundIndex);
    }

    public Color getGraphicsColour() {
        return graphicsColour;
    }

    public void paintCell(final Graphics2D g, final int v, final int x, final int y, final int width, final int height) {
        //
        // Paint background
        //
        g.setColor(background);
        g.fillRect(x, y, width, height);

        final CellProcessor processor = cellProcessorSet.getProcessor(v);
        if (processor != null) {
            processor.process(this, g, x, y, width, height);
        }
    }

    public void enableText(final int colourIndex) {
        foregroundIndex = colourIndex;
        graphicsColour = null;
        graphicsEnabled = false;
        cellProcessorSet = alphaProcessorSet;
    }

    public void enableGraphics(final int colourIndex) {
        graphicsColour = TeletextConstants.getColour(colourIndex);
        graphicsEnabled = true;
        cellProcessorSet = graphicsProcessorSet;
    }

    public void setFlashing(final boolean flashing) {
        this.flashing = flashing;
    }

    public void setDoubleHeight(final boolean doubleHeight) {
        this.doubleHeight = doubleHeight;
    }

    public void concealDisplay() {
        this.conceal = true;
    }

    public void setContiguousGraphics(final boolean contiguousGraphics) {
        this.contiguousGraphics = contiguousGraphics;
    }

    public void blackBackground() {
        background = Color.BLACK;
    }

    public void newBackground() {
        background = TeletextConstants.getColour(foregroundIndex);
    }

    public void setHoldGraphics(boolean holdGraphics) {
        this.holdGraphics = holdGraphics;
    }
}
