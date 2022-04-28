package com.jbeeb.teletext;

import java.util.Arrays;

public final class CompoundCellProcessorSet implements CellProcessorSet {
    private final CellProcessorSet[] sets;

    public CompoundCellProcessorSet(CellProcessorSet... sets) {
        this.sets = Arrays.copyOf(sets, sets.length);
    }

    @Override
    public CellProcessor getProcessor(int code) {
        for (CellProcessorSet s : sets) {
            final CellProcessor p =  s.getProcessor(code);
            if (p != null) {
                return p;
            }
        }
        return null;
    }
}
