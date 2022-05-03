package com.jbeeb.sound;

import java.util.function.IntConsumer;

public final class NopSoundChip implements IntConsumer {

    @Override
    public void accept(int value) {
        // Do nothing
    }
}
