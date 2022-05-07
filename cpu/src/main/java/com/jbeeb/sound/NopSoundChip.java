package com.jbeeb.sound;

import java.util.function.IntConsumer;

public final class NopSoundChip implements SoundChip {

    @Override
    public void accept(int value) {
        // Do nothing
    }

    @Override
    public void setPaused(boolean paused) {
        // Do nothing
    }
}
