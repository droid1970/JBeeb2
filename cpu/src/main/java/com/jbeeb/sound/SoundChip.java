package com.jbeeb.sound;

public interface SoundChip {
    void accept(final int cmd);
    void setPaused(boolean paused);
}
