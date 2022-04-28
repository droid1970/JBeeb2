package com.jbeeb.teletext;

import java.awt.*;

interface CellProcessor {
    void process(TeletextRenderer renderer, Graphics2D g, int x, int y, int width, int height);
}
