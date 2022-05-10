package com.jbeeb.sound;

import com.jbeeb.util.StateKey;

public interface SoundChip {
    void accept(final int cmd);
    void setPaused(boolean paused);
}
