package com.jbeeb.teletext;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

final class ControlCodeProcessorSet extends AbstractCellProcessorSet {

    private static final int BLACK = 0;
    private static final int RED = 1;
    private static final int GREEN = 2;
    private static final int YELLOW = 3;
    private static final int BLUE = 4;
    private static final int MAGENTA = 5;
    private static final int CYAN = 6;
    private static final int WHITE = 7;

    private static final Map<Integer, CellProcessor> MAP = new HashMap<>();
    static {

        //
        // Alphanumeric
        //
        register(129, s -> s.enableText(RED));
        register(130, s -> s.enableText(GREEN));
        register(131, s -> s.enableText(YELLOW));
        register(132, s -> s.enableText(BLUE));
        register(133, s -> s.enableText(MAGENTA));
        register(134, s -> s.enableText(CYAN));
        register(135, s -> s.enableText(WHITE));
        register(136, s -> s.enableText(RED));

        //
        // Flashing
        //
        register(136, s -> s.setFlashing(true));
        register(137, s -> s.setFlashing(false));

        //
        // Double height
        //
        register(140, s -> s.setDoubleHeight(false));
        register(141, s -> s.setDoubleHeight(true));

        //
        // Graphics
        //
        register(145, s -> s.enableGraphics(RED));
        register(146, s -> s.enableGraphics(GREEN));
        register(147, s -> s.enableGraphics(YELLOW));
        register(148, s -> s.enableGraphics(BLUE));
        register(149, s -> s.enableGraphics(MAGENTA));
        register(150, s -> s.enableGraphics(CYAN));
        register(151, s -> s.enableGraphics(WHITE));
        register(152, s -> s.enableGraphics(RED));

        //
        // Various
        //
        register(152, s -> s.concealDisplay());
        register(153, s -> s.setContiguousGraphics(true));
        register(154, s -> s.setContiguousGraphics(false));
        register(156, s -> s.blackBackground());
        register(157, s -> s.newBackground());
        register(158, s -> s.setHoldGraphics(true));
        register(159, s -> s.setHoldGraphics(false));
    }

    private static void register(final int code, final Consumer<TeletextRenderer> consumer) {
        MAP.put(code, of(consumer));
    }

    ControlCodeProcessorSet() {
        super(MAP);
    }

    private static ControlCellProcessor of(final Consumer<TeletextRenderer> stateConsumer) {
        return new ControlCellProcessor(stateConsumer);
    }

    private static final class ControlCellProcessor implements CellProcessor {

        private final Consumer<TeletextRenderer> rendererConsumer;

        ControlCellProcessor(final Consumer<TeletextRenderer> rendererConsumer) {
            this.rendererConsumer = rendererConsumer;
        }

        @Override
        public void process(TeletextRenderer renderer, Graphics2D g, int x, int y, int width, int height) {
            // TODO: held characters

            if (rendererConsumer != null) {
                rendererConsumer.accept(renderer);
            }
        }
    }
}
