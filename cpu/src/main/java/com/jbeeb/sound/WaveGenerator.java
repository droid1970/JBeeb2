package com.jbeeb.sound;

public interface WaveGenerator {

    double next();

    void setPeriod(long period);

    default void setFrequency(int frequency, int sampleRate) {
        setPeriod(Math.round((double) sampleRate / frequency / 2));
    }
}
