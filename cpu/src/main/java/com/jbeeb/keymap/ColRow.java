package com.jbeeb.keymap;

import java.util.Objects;

public final class ColRow {

    public final int col;
    public final int row;

    ColRow(final int col, final int row) {
        this.col = col;
        this.row = row;
    }

    public static ColRow of(final int col, final int row) {
        return new ColRow(col, row);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColRow colRow = (ColRow) o;
        return col == colRow.col && row == colRow.row;
    }

    @Override
    public int hashCode() {
        return Objects.hash(col, row);
    }

    public String toString() {
        return "(" + col + ", " + row + ")";
    }
}
