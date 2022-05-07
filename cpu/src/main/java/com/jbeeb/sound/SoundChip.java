package com.jbeeb.sound;

import java.util.function.IntConsumer;

public interface SoundChip {
    void accept(final int cmd);
    void setPaused(boolean paused);
}
