package com.jbeeb.keymap;

import java.util.Objects;

public final class SourceKey {

    private final int keycode;
    private final boolean shift;

    public SourceKey(final int keycode, final boolean shift) {
        this.keycode = keycode;
        this.shift = shift;
    }

    public static SourceKey of(final int keycode, final boolean shift) {
        return new SourceKey(keycode, shift);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SourceKey sourceKey = (SourceKey) o;
        return keycode == sourceKey.keycode && shift == sourceKey.shift;
    }

    @Override
    public int hashCode() {
        return Objects.hash(keycode, shift);
    }
}
