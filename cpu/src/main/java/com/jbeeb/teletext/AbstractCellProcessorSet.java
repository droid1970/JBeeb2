package com.jbeeb.teletext;

import java.util.Map;

abstract class AbstractCellProcessorSet implements CellProcessorSet {

    private final CellProcessor[] processors = new CellProcessor[256];

    AbstractCellProcessorSet(final Map<Integer, CellProcessor> map) {
        map.forEach((code, processor) -> this.processors[code] = processor);
    }

    @Override
    public final CellProcessor getProcessor(int code) {
        return processors[code & 0xFF];
    }
}
