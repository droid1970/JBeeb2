package com.jbeeb.sound;

public interface SoundChannel {
    void start();
    void setVolume(double volume);
    void setFrequency(int frequency);
}
