package com.jbeeb.teletext;

import java.util.HashMap;
import java.util.Map;

import static com.jbeeb.teletext.TeletextAlphaDefinition.*;

final class GraphicsCellProcessorSet extends AbstractCellProcessorSet {

    private static final Map<Integer, CellProcessor> MAP = new HashMap<>();
    static {

        int bits = 0;
        int code = 32;
        for (int i = 0; i < 3; i++) {
            registerGraphics(code++, bits++);
        }

        // skip 35 (which is the HASH character)
        code++;
        bits++;
        registerText(35,
                HASH
        );

        for (int i = 0; i < 28; i++) {
            registerGraphics(code++, bits++);
        }

        code = 95;
        for (int i = 0; i < 32; i++) {
            registerGraphics(code++, bits++);
        }

        bits = 0;
        code = 160;

        for (int i = 0; i < 32; i++) {
            registerGraphics(code++, bits++);
        }

        code = 224;
        for (int i = 0; i < 32; i++) {
            registerGraphics(code++, bits++);
        }

        registerText(64,
                AT,
                UPPER_A,
                UPPER_B,
                UPPER_C,
                UPPER_D,
                UPPER_E,
                UPPER_F,
                UPPER_G,
                UPPER_H,
                UPPER_I,
                UPPER_J,
                UPPER_K,
                UPPER_L,
                UPPER_M,
                UPPER_N,
                UPPER_O,
                UPPER_P,
                UPPER_Q,
                UPPER_R,
                UPPER_S,
                UPPER_T,
                UPPER_U,
                UPPER_V,
                UPPER_W,
                UPPER_X,
                UPPER_Y,
                UPPER_Z,

                LEFT_ARROW,
                HALF,
                RIGHT_ARROW,
                UP_ARROW
        );

        registerText(192,
                AT,
                UPPER_A,
                UPPER_B,
                UPPER_C,
                UPPER_D,
                UPPER_E,
                UPPER_F,
                UPPER_G,
                UPPER_H,
                UPPER_I,
                UPPER_J,
                UPPER_K,
                UPPER_L,
                UPPER_M,
                UPPER_N,
                UPPER_O,
                UPPER_P,
                UPPER_Q,
                UPPER_R,
                UPPER_S,
                UPPER_T,
                UPPER_U,
                UPPER_V,
                UPPER_W,
                UPPER_X,
                UPPER_Y,
                UPPER_Z,

                LEFT_ARROW,
                HALF,
                RIGHT_ARROW,
                UP_ARROW,

                HASH
        );

    }

    private static void registerGraphics(final int code, final int bits) {
        MAP.put(code, new GraphicsCellProcessor(bits));
    }

    private static void registerGraphics(final int code, AlphaDefinition characterSpec) {
        MAP.put(code, new TextCellProcessor(characterSpec));
    }

    private static void registerText(final int startCode, AlphaDefinition... specs) {
        int code = startCode;
        for (int i = 0; i < specs.length; i++) {
            registerGraphics(code++, specs[i]);
        }
    }

    GraphicsCellProcessorSet() {
        super(MAP);
    }
}
