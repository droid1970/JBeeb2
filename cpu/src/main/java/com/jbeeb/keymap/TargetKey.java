package com.jbeeb.keymap;

import java.util.Objects;

public final class TargetKey {

    private final int col;
    private final int row;
    private final Boolean shift; // can be null

    public TargetKey(final int col, final int row, final Boolean shift) {
        this.col = col;
        this.row = row;
        this.shift = shift;
    }

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }

    public ColRow getColRow() {
        return new ColRow(col, row);
    }

    public Boolean getShift() {
        return shift;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TargetKey targetKey = (TargetKey) o;
        return col == targetKey.col && row == targetKey.row && Objects.equals(shift, targetKey.shift);
    }

    @Override
    public int hashCode() {
        return Objects.hash(col, row, shift);
    }
}
